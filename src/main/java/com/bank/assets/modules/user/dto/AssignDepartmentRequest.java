package com.bank.assets.modules.user.dto;

import java.util.UUID;

public record AssignDepartmentRequest(
        UUID departmentId, // null = remove from department
        UUID branchId // null = remove from branch
) {}
