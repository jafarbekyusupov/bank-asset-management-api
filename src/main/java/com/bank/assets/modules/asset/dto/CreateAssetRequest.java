package com.bank.assets.modules.asset.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

public record CreateAssetRequest(
        @NotBlank(message = "Asset name is required")
        String name,

        String description,

        @NotBlank(message = "Serial number is required")
        String serialNumber,

        String brand,
        String model,

        @NotNull(message = "Category is required")
        UUID categoryId,

        @NotNull(message = "Asset type is required")
        UUID typeId,

        LocalDate purchaseDate,
        LocalDate warrantyUntil,
        BigDecimal purchasePrice,

        Map<String, Object> specifications,

        String notes
) {}
