package com.bank.assets.modules.branch.dto;

import com.bank.assets.modules.branch.Branch;

import java.time.Instant;
import java.util.UUID;

public record BranchResponse(
    UUID id,
    String name,
    String location,
    Instant createdAt
) {
    public static BranchResponse from(Branch b) {
        return new BranchResponse(b.getId(), b.getName(), b.getLocation(), b.getCreatedAt());
    }
}
