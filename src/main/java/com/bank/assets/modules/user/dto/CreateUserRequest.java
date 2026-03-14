package com.bank.assets.modules.user.dto;

import com.bank.assets.common.enums.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateUserRequest(
        @NotBlank String fullName,
        @NotBlank @Email String email,
        @NotNull UserRole role,
        UUID deptId,
        UUID branchId
) {}
