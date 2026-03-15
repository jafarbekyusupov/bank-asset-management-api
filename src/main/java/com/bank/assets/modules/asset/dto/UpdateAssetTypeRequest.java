package com.bank.assets.modules.asset.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateAssetTypeRequest(
        @NotBlank String name,
        String description
) {}
