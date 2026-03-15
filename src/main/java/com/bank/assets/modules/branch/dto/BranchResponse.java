package com.bank.assets.modules.branch.dto;

import com.bank.assets.modules.branch.Branch;

import java.time.Instant;
import java.util.UUID;

public record BranchResponse(
    UUID id,
    String name,
    String location,
    Instant createdAt,
    boolean isActive,
    Instant archivedAt,
    Boolean canDelete
) {
    public static BranchResponse from(Branch b) {
        return new BranchResponse(
            b.getId(), 
            b.getName(), 
            b.getLocation(), 
            b.getCreatedAt(),
            b.isActive(), 
            b.getArchivedAt(), 
            null
        );
    }

    public static BranchResponse from(Branch b, boolean canDelete) {
        return new BranchResponse(
            b.getId(), 
            b.getName(), 
            b.getLocation(), 
            b.getCreatedAt(),
            b.isActive(), 
            b.getArchivedAt(), 
            canDelete
        );
    }
}
