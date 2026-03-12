package com.bank.assets.modules.assignment.dto;

import com.bank.assets.common.enums.AssetStatus;
import jakarta.validation.constraints.NotNull;

public record ChangeStatusRequest(
        @NotNull(message = "New status is required")
        AssetStatus newStatus,

        String reason // required for LOST, WRITTEN_OFF statuses
) {}
