package com.bank.assets.modules.assignment.dto;
import java.util.UUID;

public record AssignAssetRequest(
        UUID assignedToUserId,
        UUID assignedToDeptId,
        String notes
) {}
