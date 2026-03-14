package com.bank.assets.modules.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

public record UpdateUserRequest(
        @NotBlank String fullName,
        @NotBlank @Email String email,
        UUID deptId,
        UUID branchId
) {}
