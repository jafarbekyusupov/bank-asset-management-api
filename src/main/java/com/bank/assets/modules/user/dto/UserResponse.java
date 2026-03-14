package com.bank.assets.modules.user.dto;

import com.bank.assets.common.enums.UserRole;
import com.bank.assets.common.enums.UserStatus;
import com.bank.assets.modules.user.User;

import java.time.Instant;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String fullName,
        String email,
        UserRole role,
        UserStatus status,
        boolean isDev,
        DeptInfo department,
        BranchInfo branch,
        Instant createdAt,
        Instant updatedAt
) {
    public record DeptInfo(UUID id, String name) {}
    public record BranchInfo(UUID id, String name) {}

    public static UserResponse from(User u) {
        return new UserResponse(
                u.getId(),
                u.getFullName(),
                u.getEmail(),
                u.getRole(),
                u.getStatus(),
                u.isDev(),
                u.getDepartment() != null
                        ? new DeptInfo(u.getDepartment().getId(), u.getDepartment().getName())
                        : null,
                u.getBranch() != null
                        ? new BranchInfo(u.getBranch().getId(), u.getBranch().getName())
                        : null,
                u.getCreatedAt(),
                u.getUpdatedAt()
        );
    }
}
