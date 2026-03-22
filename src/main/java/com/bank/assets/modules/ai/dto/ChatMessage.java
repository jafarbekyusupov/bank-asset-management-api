package com.bank.assets.modules.ai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record ChatMessage(
        @Pattern(regexp = "user|model", message = "role must be 'user' or 'model'")
        String role,

        @NotBlank
        String content
) {}
