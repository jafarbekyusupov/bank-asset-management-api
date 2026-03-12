package com.bank.assets.modules.user.dto;

import com.bank.assets.common.enums.UserStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateUserStatusRequest(
        @NotNull(message = "Status is required")
        UserStatus newStatus
) {}
