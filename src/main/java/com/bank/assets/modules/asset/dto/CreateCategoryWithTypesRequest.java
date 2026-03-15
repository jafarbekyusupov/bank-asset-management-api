package com.bank.assets.modules.asset.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CreateCategoryWithTypesRequest(
        @NotBlank String name,
        String description,
        @NotNull @Valid List<TypeEntry> types
) {
    public record TypeEntry(
        @NotBlank String name,
        String description
    ) {}
}
