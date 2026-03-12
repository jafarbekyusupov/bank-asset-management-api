package com.bank.assets.modules.assignment.dto;

import com.bank.assets.common.enums.AssignmentRequestStatus;
import com.bank.assets.modules.assignment.AssignmentRequest;

import java.time.Instant;
import java.util.UUID;

public record AssignmentRequestResponse(
        UUID id,
        AssetInfo asset,
        UserInfo requestedBy,
        String reason,
        AssignmentRequestStatus status,
        UserInfo reviewedBy,
        String adminNote,
        Instant createdAt,
        Instant reviewedAt
) {
    public record AssetInfo(UUID id, String name, String serialNumber) {}
    public record UserInfo(UUID id, String fullName, String email) {}

    public static AssignmentRequestResponse from(AssignmentRequest r) {
        return new AssignmentRequestResponse(
                r.getId(),
                new AssetInfo(r.getAsset().getId(), r.getAsset().getName(), r.getAsset().getSerialNumber()),
                new UserInfo(r.getRequestedBy().getId(), r.getRequestedBy().getFullName(), r.getRequestedBy().getEmail()),
                r.getReason(),
                r.getStatus(),
                r.getReviewedBy() != null 
                        ? new UserInfo(r.getReviewedBy().getId(), r.getReviewedBy().getFullName(), r.getReviewedBy().getEmail())
                        : null,
                r.getAdminNote(),
                r.getCreatedAt(),
                r.getReviewedAt()
        );
    }
}
