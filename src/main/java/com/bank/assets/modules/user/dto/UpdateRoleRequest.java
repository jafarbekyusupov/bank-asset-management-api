package com.bank.assets.modules.user.dto;

import com.bank.assets.common.enums.UserRole;
import jakarta.validation.constraints.NotNull;

public record UpdateRoleRequest(
        @NotNull UserRole role
) {}
