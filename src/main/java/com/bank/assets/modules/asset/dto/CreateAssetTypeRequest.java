package com.bank.assets.modules.asset.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateAssetTypeRequest(
        @NotBlank String name,
        String description,
        @NotNull UUID categoryId
) {}
