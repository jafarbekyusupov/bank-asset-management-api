package com.bank.assets.modules.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ResendOtpRequest(
        @Email(message = "Valid email is required")
        @NotBlank(message = "Email is required")
        String email
) {}
