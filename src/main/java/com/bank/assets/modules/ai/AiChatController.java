package com.bank.assets.modules.ai;

import com.bank.assets.common.response.ApiResponse;
import com.bank.assets.common.response.PageResponse;
import com.bank.assets.modules.ai.dto.MessageResponse;
import com.bank.assets.modules.ai.dto.SendMessageRequest;
import com.bank.assets.modules.ai.dto.SessionResponse;
import com.bank.assets.modules.ai.dto.ChatResponse;
import com.bank.assets.modules.user.User;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/ai")
@Tag(name = "AI Chat")
@RequiredArgsConstructor
public class AiChatController {
    private final AiSessionService sessionService;
    private final AiChatService chatService;

    @PostMapping("/session")
    public ResponseEntity<ApiResponse<SessionResponse>> createSession(
        @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.ok("Session created.", sessionService.createSession(user)));
    }

    @GetMapping("/session/list")
    public ResponseEntity<ApiResponse<PageResponse<SessionResponse>>> listSessions(
        @AuthenticationPrincipal User user,
        @PageableDefault(size = 20, sort = "updatedAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
            PageResponse.from(sessionService.listSessions(user, pageable))));
    }

    @GetMapping("/session/{id}/messages")
    public ResponseEntity<ApiResponse<List<MessageResponse>>> getMessages(
        @PathVariable UUID id,
        @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(ApiResponse.ok(sessionService.getMessages(id, user)));
    }

    /*
    * req/resp - waits for full ai response (including tool calls) before returning
    */
    @PostMapping("/chat")
    public ResponseEntity<ApiResponse<ChatResponse>> chat(
        @Valid @RequestBody SendMessageRequest req,
        @AuthenticationPrincipal User user
    ) {
        if (user.getDepartment() != null) user.getDepartment().getName();
        if (user.getBranch() != null) user.getBranch().getName();
        return ResponseEntity.ok(ApiResponse.ok(chatService.sendMessage(req, user)));
    }

    /**
     * sse streaming - tool calls resolve silently; 
     * final answer streams token by token
     * each sse event - `data: <token>`
     * completion event - `event: done\ndata: \n\n`
     */
    @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream(
        @Valid @RequestBody SendMessageRequest req,
        @AuthenticationPrincipal User user
    ) {
        SseEmitter emitter = new SseEmitter(90_000L);

        if (user.getDepartment() != null) user.getDepartment().getName();
        if (user.getBranch() != null) user.getBranch().getName();

        CompletableFuture.runAsync(() -> chatService.streamMessage(req, user, emitter));
        return emitter;
    }
}
