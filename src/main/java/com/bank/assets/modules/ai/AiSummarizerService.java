package com.bank.assets.modules.ai;

import com.bank.assets.modules.asset.AssetService;
import com.bank.assets.modules.asset.dto.AssetNoteResponse;
import com.bank.assets.modules.asset.dto.AssetResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class AiSummarizerService {
    @Value("${app.gemini.base-url}")
    private String geminiBaseUrl;

    @Value("${app.gemini.api-key}")
    private String apiKey;

    private final AssetService assetService;
    private final ObjectMapper objectMapper;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter
        .ofPattern("yyyy-MM-dd")
        .withZone(ZoneId.of("UTC"));

    public String summarize(UUID assetId) {
        AssetResponse asset = assetService.getById(assetId);
        List<AssetNoteResponse> notes = assetService.getNotes(assetId);

        if (notes.isEmpty()) {
            return "No recorded issues or feedback found for this asset yet.";
        }

        String prompt = buildPrompt(asset, notes);

        ObjectNode body = objectMapper.createObjectNode();
        body.putArray("contents")
            .addObject()
            .put("role", "user")
            .putArray("parts")
            .addObject()
            .put("text", prompt);
        body.putObject("generationConfig")
            .put("temperature", 0.3)
            .put("maxOutputTokens", 1024);

        String raw = RestClient.create()
            .post()
            .uri(geminiBaseUrl + ":generateContent?key=" + apiKey)
            .contentType(MediaType.APPLICATION_JSON)
            .body(body.toString())
            .retrieve()
            .body(String.class);

        try {
            JsonNode response = objectMapper.readTree(raw);
            return response.path("candidates").path(0)
                .path("content").path("parts").path(0)
                .path("text").asText("Unable to generate summary.");
        } catch (Exception e) {
            log.error("Failed to parse Gemini summarizer response: {}", e.getMessage());
            return "Unable to generate summary.";
        }
    }

    private String buildPrompt(AssetResponse asset, List<AssetNoteResponse> notes) {
        StringBuilder sb = new StringBuilder();
        sb.append("You are an asset management assistant for a bank.\n");
        sb.append("Below are recorded events and user feedback for the following asset:\n\n");
        sb.append("Asset: ").append(asset.name()).append("\n");
        sb.append("Serial: ").append(asset.serialNumber()).append("\n");
        if (asset.type() != null) sb.append("Type: ").append(asset.type().name()).append("\n");
        if (asset.brand() != null) sb.append("Brand: ").append(asset.brand()).append("\n");
        sb.append("\nRecorded notes (").append(notes.size()).append(" total):\n\n");

        for (int i = 0; i < notes.size(); i++) {
            AssetNoteResponse note = notes.get(i);
            sb.append(i + 1).append(". [").append(note.type()).append("]");
            if (note.action() != null) sb.append(" (").append(note.action()).append(")");
            sb.append("\n");
            if (note.oldStatus() != null && note.newStatus() != null) {
                sb.append(" Status: ").append(note.oldStatus()).append(" -> ").append(note.newStatus()).append("\n");
            }
            sb.append(" Content: ").append(note.content()).append("\n");
            sb.append(" By: ").append(note.author().fullName()).append("\n");
            sb.append(" Date: ").append(DATE_FMT.format(note.date())).append("\n\n");
        }

        sb.append("Based on these records, write a short honest summary (2-4 sentences) of whether this asset is ");
        sb.append("reliable and worth requesting. Highlight any recurring issues or patterns. ");
        sb.append("Be direct and practical. Do not restate the asset name or serial number — go straight to the verdict.");
        return sb.toString();
    }
}
