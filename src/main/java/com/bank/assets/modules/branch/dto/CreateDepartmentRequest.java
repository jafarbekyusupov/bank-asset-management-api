package com.bank.assets.modules.branch.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateDepartmentRequest(
        @NotBlank(message = "Department name is required")
        String name
) {}
