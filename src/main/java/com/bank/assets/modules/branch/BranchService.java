package com.bank.assets.modules.branch;

import com.bank.assets.common.exception.AppException;
import com.bank.assets.common.exception.ErrorCode;
import com.bank.assets.modules.asset.AssetRepository;
import com.bank.assets.modules.branch.dto.BranchResponse;
import com.bank.assets.modules.branch.dto.CreateBranchRequest;
import com.bank.assets.modules.branch.dto.CreateDepartmentRequest;
import com.bank.assets.modules.branch.dto.DepartmentResponse;
import com.bank.assets.modules.branch.dto.UpdateBranchRequest;
import com.bank.assets.modules.branch.dto.UpdateDepartmentRequest;
import com.bank.assets.modules.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BranchService {
    private final BranchRepository branchRepository;
    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;
    private final AssetRepository assetRepository;

    @Transactional(readOnly = true)
    public List<BranchResponse> listBranches() {
        return branchRepository
            .findAllByIsActiveTrue()
            .stream()
            .map(BranchResponse::from)
            .toList();
    }

    @Transactional
    public BranchResponse createBranch(CreateBranchRequest req) {
        Branch branch = Branch.builder()
            .name(req.name())
            .location(req.location())
            .build();
        return BranchResponse.from(branchRepository.save(branch));
    }

    @Transactional(readOnly = true)
    public BranchResponse getBranch(UUID id) {
        Branch branch = findBranchOrThrow(id);
        return BranchResponse.from(branch, canDeleteBranch(id));
    }

    @Transactional
    public BranchResponse updateBranch(UUID id, UpdateBranchRequest req) {
        Branch branch = findBranchOrThrow(id);
        branch.setName(req.name());
        branch.setLocation(req.location());
        return BranchResponse.from(branchRepository.save(branch));
    }

    @Transactional
    public void archiveBranch(UUID id) {
        Branch branch = findBranchOrThrow(id);
        if (departmentRepository.existsByBranchIdAndIsActiveTrue(id)) {
            throw AppException.conflict(ErrorCode.BRANCH_HAS_DEPARTMENTS);
        }
        if (userRepository.existsByBranchId(id)) {
            throw AppException.conflict(ErrorCode.BRANCH_HAS_USERS);
        }
        if (assetRepository.existsByBranchId(id)) {
            throw AppException.conflict(ErrorCode.BRANCH_HAS_ASSETS);
        }
        branch.setActive(false);
        branch.setArchivedAt(Instant.now());
        branchRepository.save(branch);
    }

    @Transactional(readOnly = true)
    public List<DepartmentResponse> listDepartments(UUID branchId) {
        findBranchOrThrow(branchId);
        return departmentRepository
            .findByBranchIdAndIsActiveTrue(branchId)
            .stream()
            .map(DepartmentResponse::from)
            .toList();
    }

    @Transactional
    public DepartmentResponse createDepartment(UUID branchId, CreateDepartmentRequest req) {
        Branch branch = findBranchOrThrow(branchId);
        Department dept = Department.builder()
            .name(req.name())
            .branch(branch)
            .build();
        return DepartmentResponse.from(departmentRepository.save(dept));
    }

    @Transactional(readOnly = true)
    public DepartmentResponse getDepartment(UUID deptId) {
        Department dept = findDeptOrThrow(deptId);
        return DepartmentResponse.from(dept, canDeleteDepartment(deptId));
    }

    @Transactional
    public DepartmentResponse updateDepartment(UUID deptId, UpdateDepartmentRequest req) {
        Department dept = findDeptOrThrow(deptId);
        dept.setName(req.name());
        return DepartmentResponse.from(departmentRepository.save(dept));
    }

    @Transactional
    public void archiveDepartment(UUID deptId) {
        Department dept = findDeptOrThrow(deptId);
        if (userRepository.existsByDepartmentId(deptId)) {
            throw AppException.conflict(ErrorCode.DEPARTMENT_HAS_USERS);
        }
        if (assetRepository.existsByDepartmentId(deptId)) {
            throw AppException.conflict(ErrorCode.DEPARTMENT_HAS_ASSETS);
        }
        dept.setActive(false);
        dept.setArchivedAt(Instant.now());
        departmentRepository.save(dept);
    }

    @Transactional(readOnly = true)
    public List<DepartmentResponse> listAllDepartments() {
        return departmentRepository.findAll().stream()
            .filter(Department::isActive)
            .map(DepartmentResponse::from)
            .toList();
    }

    private boolean canDeleteBranch(UUID id) {
        return !departmentRepository.existsByBranchIdAndIsActiveTrue(id)
                && !userRepository.existsByBranchId(id)
                && !assetRepository.existsByBranchId(id);
    }

    private boolean canDeleteDepartment(UUID id) {
        return !userRepository.existsByDepartmentId(id)
                && !assetRepository.existsByDepartmentId(id);
    }

    private Branch findBranchOrThrow(UUID id) {
        return branchRepository
            .findById(id)
            .orElseThrow(() -> AppException.notFound(ErrorCode.BRANCH_NOT_FOUND));
    }

    private Department findDeptOrThrow(UUID id) {
        return departmentRepository
            .findById(id)
            .orElseThrow(() -> AppException.notFound(ErrorCode.DEPARTMENT_NOT_FOUND));
    }
}
