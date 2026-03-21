package com.bank.assets.modules.user.dto;

import com.bank.assets.common.enums.UserRole;
import com.bank.assets.common.enums.UserStatus;
import jakarta.validation.constraints.Email;

import java.util.UUID;

public record UpdateUserRequest(
        String fullName,
        @Email String email,
        UUID deptId,
        UUID branchId,
        UserRole role,
        UserStatus status
) {}
