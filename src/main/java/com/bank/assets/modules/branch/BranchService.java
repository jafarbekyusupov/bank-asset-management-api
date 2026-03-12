package com.bank.assets.modules.branch;

import com.bank.assets.common.exception.AppException;
import com.bank.assets.common.exception.ErrorCode;
import com.bank.assets.modules.branch.dto.BranchResponse;
import com.bank.assets.modules.branch.dto.CreateBranchRequest;
import com.bank.assets.modules.branch.dto.CreateDepartmentRequest;
import com.bank.assets.modules.branch.dto.DepartmentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BranchService {
    private final BranchRepository branchRepository;
    private final DepartmentRepository departmentRepository;

    @Transactional(readOnly = true)
    public List<BranchResponse> listBranches() {
        return branchRepository.findAll().stream().map(BranchResponse::from).toList();
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
        return BranchResponse.from(findBranchOrThrow(id));
    }

    @Transactional(readOnly = true)
    public List<DepartmentResponse> listDepartments(UUID branchId) {
        findBranchOrThrow(branchId);
        return departmentRepository
            .findByBranchId(branchId)
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
    public List<DepartmentResponse> listAllDepartments() {
        return departmentRepository
            .findAll()
            .stream()
            .map(DepartmentResponse::from)
            .toList();
    }

    private Branch findBranchOrThrow(UUID id) {
        return branchRepository
            .findById(id)
            .orElseThrow(() -> AppException.notFound(ErrorCode.BRANCH_NOT_FOUND));
    }
}
