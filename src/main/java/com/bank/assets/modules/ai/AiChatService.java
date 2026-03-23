package com.bank.assets.modules.ai;

import com.bank.assets.common.exception.AppException;
import com.bank.assets.common.exception.ErrorCode;
import com.bank.assets.modules.ai.dto.ChatResponse;
import com.bank.assets.modules.ai.dto.SendMessageRequest;
import com.bank.assets.modules.user.User;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class AiChatService {
    @Value("${app.gemini.base-url}")
    private String geminiBaseUrl;

    @Value("${app.gemini.context-window}")
    private int contextWindow;

    @Value("${app.gemini.max-tool-rounds}")
    private int maxToolRounds;

    @Value("${app.gemini.api-key}")
    private String apiKey;

    private final ChatSessionRepository sessionRepository;
    private final ChatMessageRepository messageRepository;
    private final AiToolRegistry toolRegistry;
    private final ObjectMapper objectMapper;

    @Transactional
    public ChatResponse sendMessage(SendMessageRequest req, User user) {
        ChatSession session = loadAndVerify(req.sessionId(), user);

        saveMessage(session, ChatRole.user, req.message());

        ArrayNode contents = buildContentsFromDB(session, req.message());
        LinkedHashMap<String, UUID> seen = new LinkedHashMap<>();
        String responseText = runAgenticLoop(contents, user, seen);

        saveMessage(session, ChatRole.model, responseText);
        touchSession(session);

        return new ChatResponse(responseText, new ArrayList<>(seen.values()));
    }


    public void streamMessage(SendMessageRequest req, User user, SseEmitter emitter) {
        ChatSession session = loadAndVerify(req.sessionId(), user);
        saveMessage(session, ChatRole.user, req.message());

        ArrayNode contents = buildContentsFromDB(session, req.message());
        StringBuilder accumulated = new StringBuilder();
        LinkedHashMap<String, UUID> seen = new LinkedHashMap<>();

        try {
            for (int round = 0; round < maxToolRounds; round++) {
                boolean hadToolCalls = streamOneRound(contents, user, emitter, accumulated, seen);
                if (!hadToolCalls) break;
                accumulated.setLength(0);
            }
            if (!accumulated.isEmpty()) {
                saveMessage(session, ChatRole.model, accumulated.toString());
                touchSession(session);
            }
            if (!seen.isEmpty()) {
                emitter.send(SseEmitter.event().name("assets")
                    .data(objectMapper.writeValueAsString(seen.values())));
            }
            emitter.send(SseEmitter.event().name("done").data(""));
            emitter.complete();
        } catch (Exception e) {
            log.error("stream error for session {}: {}", req.sessionId(), e.getMessage());
            emitter.completeWithError(e);
        }
    }

    // non streaming
    private String runAgenticLoop(ArrayNode contents, User user, LinkedHashMap<String, UUID> seen) {
        for (int round = 0; round < maxToolRounds; round++) {
            String raw = callGemini(":generateContent", buildBody(contents, user));
            JsonNode response = parseJson(raw);
            JsonNode parts = response.path("candidates").path(0).path("content").path("parts");

            if (parts.path(0).has("functionCall")) {
                appendToolRound(contents, parts, user, seen);
                continue;
            }
            return parts.path(0).path("text").asText("");
        }
        return "I wasn't able to complete the request after several attempts.";
    }

    /**
     * @return 
     * - true: if need another round (this round had function calls)
     * - false: if streaming complete (text response)
     */
    private boolean streamOneRound(
        ArrayNode contents, User user,
        SseEmitter emitter, StringBuilder accumulated,
        LinkedHashMap<String, UUID> seen
    ) throws Exception {
        ArrayNode functionCallParts = objectMapper.createArrayNode();
        String url = geminiBaseUrl + ":streamGenerateContent?alt=sse&key=" + apiKey;
        RestClient.create()
            .post()
            .uri(url)
            .contentType(MediaType.APPLICATION_JSON)
            .body(buildBody(contents, user).toString())
            .exchange((httpReq, httpRes) -> {
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(httpRes.getBody()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        if (!line.startsWith("data: ")) continue;
                        String json = line.substring(6).trim();
                        JsonNode chunk = parseJson(json);
                        JsonNode parts = chunk.path("candidates").path(0)
                            .path("content").path("parts");

                        for (JsonNode part : parts) {
                            if (part.has("text")) {
                                String token = part.path("text").asText();
                                accumulated.append(token);
                                emitter.send(SseEmitter.event().data(token));
                            } else if (part.has("functionCall")) {
                                functionCallParts.add(part);
                            }
                        }
                    }
                }
                return null;
            });

        if (functionCallParts.isEmpty()) return false;

        ObjectNode modelTurn = objectMapper.createObjectNode();
        modelTurn.put("role", "model");
        modelTurn.set("parts", functionCallParts);
        contents.add(modelTurn);

        ObjectNode userTurn = objectMapper.createObjectNode();
        userTurn.put("role", "user");
        ArrayNode responseParts = userTurn.putArray("parts");

        for (JsonNode part : functionCallParts) {
            JsonNode fc = part.path("functionCall");
            String name = fc.path("name").asText();
            Object result = toolRegistry.execute(name, fc.path("args"), user);
            collectAssetIds(result, seen);
            responseParts.addObject()
                .putObject("functionResponse")
                .put("name", name)
                .set("response", wrapToolResult(result));
        }
        contents.add(userTurn);
        return true;
    }

    private void appendToolRound(ArrayNode contents, JsonNode parts, User user, LinkedHashMap<String, UUID> seen) {
        ObjectNode modelTurn = objectMapper.createObjectNode();
        modelTurn.put("role", "model");
        modelTurn.set("parts", parts.deepCopy());
        contents.add(modelTurn);

        ObjectNode userTurn = objectMapper.createObjectNode();
        userTurn.put("role", "user");
        ArrayNode responseParts = userTurn.putArray("parts");

        for (JsonNode part : parts) {
            if (!part.has("functionCall")) continue;
            JsonNode fc = part.path("functionCall");
            String name = fc.path("name").asText();
            Object result = toolRegistry.execute(name, fc.path("args"), user);
            collectAssetIds(result, seen);
            responseParts.addObject()
                .putObject("functionResponse")
                .put("name", name)
                .set("response", wrapToolResult(result));
        }
        contents.add(userTurn);
    }

    @SuppressWarnings("unchecked")
    private void collectAssetIds(Object result, LinkedHashMap<String, UUID> seen) {
        if (result instanceof List<?> list) {
            for (Object item : list) {
                if (item instanceof Map<?, ?> map && map.containsKey("id") && map.containsKey("serialNumber")) {
                    String sn = map.get("serialNumber").toString();
                    seen.putIfAbsent(sn, UUID.fromString(map.get("id").toString()));
                }
            }
        } else if (result instanceof Map<?, ?> map && map.containsKey("id") && map.containsKey("serialNumber")) {
            String sn = map.get("serialNumber").toString();
            seen.putIfAbsent(sn, UUID.fromString(map.get("id").toString()));
        }
    }

    private ObjectNode wrapToolResult(Object result) {
        JsonNode node = objectMapper.valueToTree(result);
        if (node.isObject()) {
            return (ObjectNode) node;
        }
        ObjectNode wrapper = objectMapper.createObjectNode();
        wrapper.set("content", node);
        return wrapper;
    }

    private ArrayNode buildContentsFromDB(ChatSession session, String newUserMessage) {
        List<ChatMessage> history = messageRepository
                .findLastN(session.getId(), PageRequest.of(0, contextWindow));
        Collections.reverse(history);

        ArrayNode contents = objectMapper.createArrayNode();
        for (ChatMessage msg : history) {
            contents.addObject()
                .put("role", msg.getRole().name())
                .putArray("parts")
                .addObject()
                .put("text", msg.getContent());
        }
        
        contents.addObject()
            .put("role", "user")
            .putArray("parts")
            .addObject()
            .put("text", newUserMessage);
        return contents;
    }

    private ObjectNode buildBody(ArrayNode contents, User user) {
        ObjectNode root = objectMapper.createObjectNode();

        root.putObject("system_instruction")
            .putArray("parts")
            .addObject()
            .put("text", buildSystemPrompt(user));

        root.set("contents", contents);

        root.putArray("tools")
            .addObject()
            .set("function_declarations", toolRegistry.buildDeclarations(user));

        root.putObject("generationConfig")
            .put("temperature", 0.7)
            .put("maxOutputTokens", 1024);

        return root;
    }

    private String buildSystemPrompt(User user) {
        StringBuilder sb = new StringBuilder();
        sb.append("You are an intelligent asset management assistant for a bank. ");
        sb.append("You have access to real-time tools to query the asset database - always use them when the user asks about assets, availability, equipment, or history. ");
        sb.append("Never say 'check the app' when you can look it up directly.\n\n");

        sb.append("Current user:\n");
        sb.append("- Name: ").append(user.getFullName()).append("\n");
        sb.append("- Role: ").append(user.getRole()).append("\n");
        if (user.getDepartment() != null)
            sb.append("- Department: ").append(user.getDepartment().getName()).append("\n");
        if (user.getBranch() != null)
            sb.append("- Branch: ").append(user.getBranch().getName()).append("\n");

        sb.append("\nBe concise and practical. ");
        sb.append("When recommending available assets, always include the serial number so the user can identify them. ");
        sb.append("Asset statuses: REGISTERED = available, ASSIGNED = in use, IN_REPAIR, LOST, WRITTEN_OFF.");
        return sb.toString();
    }

    private ChatSession loadAndVerify(java.util.UUID sessionId, User user) {
        return sessionRepository
            .findByIdAndUserId(sessionId, user.getId())
            .orElseThrow(() -> AppException.notFound(ErrorCode.SESSION_NOT_FOUND));
    }

    private void saveMessage(ChatSession session, ChatRole role, String content) {
        messageRepository.save(ChatMessage.builder()
            .session(session)
            .role(role)
            .content(content)
            .build());

        // title session from first user message
        if (role == ChatRole.user && session.getTitle() == null) {
            session.setTitle(content.length() > 60 ? content.substring(0, 57) + "..." : content);
            sessionRepository.save(session);
        }
    }

    private void touchSession(ChatSession session) {
        session.setUpdatedAt(Instant.now());
        sessionRepository.save(session);
    }

    private String callGemini(String path, ObjectNode body) {
        return RestClient.create()
            .post()
            .uri(geminiBaseUrl + path + "?key=" + apiKey)
            .contentType(MediaType.APPLICATION_JSON)
            .body(body.toString())
            .retrieve()
            .body(String.class);
    }

    private JsonNode parseJson(String raw) {
        try {
            return objectMapper.readTree(raw);
        } catch (Exception e) {
            throw new RuntimeException("failed to parse Gemini response: " + e.getMessage());
        }
    }
}
