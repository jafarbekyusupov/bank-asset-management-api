package com.bank.assets.modules.ai.dto;

import java.util.List;
import java.util.UUID;

public record ChatResponse(String content, List<UUID> assetIds) {}
