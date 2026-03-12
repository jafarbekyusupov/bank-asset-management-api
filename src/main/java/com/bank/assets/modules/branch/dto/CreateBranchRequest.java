package com.bank.assets.modules.branch.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateBranchRequest(
        @NotBlank(message = "Branch name is required")
        String name,

        String location
) {}
