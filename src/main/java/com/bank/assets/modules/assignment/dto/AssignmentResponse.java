package com.bank.assets.modules.assignment.dto;

import com.bank.assets.modules.assignment.AssetAssignment;

import java.time.Instant;
import java.util.UUID;

public record AssignmentResponse(
        UUID id,
        AssetInfo asset,
        UserInfo assignedToUser,
        DeptInfo assignedToDept,
        UserInfo assignedBy,
        Instant assignedAt,
        Instant acknowledgedAt,
        Instant returnedAt,
        String notes,
        String returnNotes
) {
    public record AssetInfo(UUID id, String name, String serialNumber) {}
    public record UserInfo(UUID id, String fullName, String email) {}
    public record DeptInfo(UUID id, String name) {}

    public static AssignmentResponse from(AssetAssignment a) {
        return new AssignmentResponse(
                a.getId(),
                new AssetInfo(a.getAsset().getId(), a.getAsset().getName(), a.getAsset().getSerialNumber()),
                a.getAssignedToUser() != null
                        ? new UserInfo(a.getAssignedToUser().getId(), a.getAssignedToUser().getFullName(), a.getAssignedToUser().getEmail()) 
                        : null,
                a.getAssignedToDept() != null
                        ? new DeptInfo(a.getAssignedToDept().getId(), a.getAssignedToDept().getName()) 
                        : null,
                a.getAssignedBy() != null
                        ? new UserInfo(a.getAssignedBy().getId(), a.getAssignedBy().getFullName(), a.getAssignedBy().getEmail()) 
                        : null,
                a.getAssignedAt(),
                a.getAcknowledgedAt(),
                a.getReturnedAt(),
                a.getNotes(),
                a.getReturnNotes()
        );
    }
}
