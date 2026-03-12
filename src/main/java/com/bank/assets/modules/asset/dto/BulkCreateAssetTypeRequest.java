package com.bank.assets.modules.asset.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record BulkCreateAssetTypeRequest(
    @NotNull UUID categoryId,
    @NotEmpty @Valid List<TypeEntry> types
) {
    public record TypeEntry(@NotNull String name, String description) {}
}
