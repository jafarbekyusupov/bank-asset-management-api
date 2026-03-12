package com.bank.assets.modules.asset.dto;

import java.util.UUID;

public record AssetTypeResponse(
        UUID id,
        String name,
        String description,
        UUID categoryId,
        String categoryName
) {}
