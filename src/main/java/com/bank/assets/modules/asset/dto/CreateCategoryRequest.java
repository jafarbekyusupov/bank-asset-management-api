package com.bank.assets.modules.asset.dto;
import jakarta.validation.constraints.NotBlank;

public record CreateCategoryRequest(
        @NotBlank String name,
        String description
) {}
