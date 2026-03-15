package com.bank.assets.modules.branch;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface DepartmentRepository extends JpaRepository<Department, UUID> {
    List<Department> findByBranchIdAndIsActiveTrue(UUID branchId);
    boolean existsByBranchIdAndIsActiveTrue(UUID branchId);
    boolean existsByBranchId(UUID branchId);
}
