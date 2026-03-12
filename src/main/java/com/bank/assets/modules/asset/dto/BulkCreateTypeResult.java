package com.bank.assets.modules.asset.dto;
import java.util.List;

public record BulkCreateTypeResult(
        List<AssetTypeResponse> created,
        List<String> skipped
) {}
