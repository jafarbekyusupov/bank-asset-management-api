package com.bank.assets.modules.asset.dto;

import java.util.UUID;

public record CategoryResponse(
        UUID id,
        String name,
        String description,
        Boolean canDelete
) {
    public CategoryResponse(UUID id, String name, String description) {
        this(id, name, description, null);
    }
}
