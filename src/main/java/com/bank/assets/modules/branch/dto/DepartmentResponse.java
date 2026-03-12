package com.bank.assets.modules.branch.dto;

import com.bank.assets.modules.branch.Department;

import java.time.Instant;
import java.util.UUID;

public record DepartmentResponse(
    UUID id,
    String name,
    UUID branchId,
    String branchName,
    Instant createdAt
) {
    public static DepartmentResponse from(Department d) {
        return new DepartmentResponse(
            d.getId(),
            d.getName(),
            d.getBranch() != null ? d.getBranch().getId() : null,
            d.getBranch() != null ? d.getBranch().getName() : null,
            d.getCreatedAt()
        );
    }
}
