package com.bank.assets.modules.ai.dto;

import com.bank.assets.modules.ai.ChatSession;

import java.time.Instant;
import java.util.UUID;

public record SessionResponse(
    UUID id,
    String title,
    Instant createdAt,
    Instant updatedAt
) {
    public static SessionResponse from(ChatSession s) {
        return new SessionResponse(s.getId(), s.getTitle(), s.getCreatedAt(), s.getUpdatedAt());
    }
}
