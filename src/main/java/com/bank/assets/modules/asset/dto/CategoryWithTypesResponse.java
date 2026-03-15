package com.bank.assets.modules.asset.dto;

import java.util.List;
import java.util.UUID;

public record CategoryWithTypesResponse(
        UUID id,
        String name,
        String description,
        List<AssetTypeResponse> types
) {}
