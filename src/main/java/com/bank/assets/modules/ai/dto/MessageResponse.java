package com.bank.assets.modules.ai.dto;

import com.bank.assets.modules.ai.ChatMessage;
import com.bank.assets.modules.ai.ChatRole;

import java.time.Instant;
import java.util.UUID;

public record MessageResponse(
    UUID id,
    ChatRole role,
    String content,
    Instant createdAt
) {
    public static MessageResponse from(ChatMessage m) {
        return new MessageResponse(m.getId(), m.getRole(), m.getContent(), m.getCreatedAt());
    }
}
