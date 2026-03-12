package com.bank.assets.modules.asset.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

public record UpdateAssetRequest(
        String name,
        String description,
        String brand,
        String model,
        UUID typeId,
        LocalDate purchaseDate,
        LocalDate warrantyUntil,
        BigDecimal purchasePrice,
        Map<String, Object> specifications,
        String notes
) {}
