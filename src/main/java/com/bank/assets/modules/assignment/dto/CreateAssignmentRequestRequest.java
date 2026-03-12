package com.bank.assets.modules.assignment.dto;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record CreateAssignmentRequestRequest(
        @NotNull UUID assetId,
        String reason
) {}
