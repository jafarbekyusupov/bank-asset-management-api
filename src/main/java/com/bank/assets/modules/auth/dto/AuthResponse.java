package com.bank.assets.modules.auth.dto;

import com.bank.assets.common.enums.UserRole;
import com.bank.assets.common.enums.UserStatus;

import java.util.UUID;

public record AuthResponse(
        String accessToken,
        String tokenType,
        long expiresIn,
        UserInfo user
) {
    public record UserInfo(
        UUID id,
        String fullName,
        String email,
        UserRole role,
        UserStatus status
    ) {}
}
