package com.bank.assets.modules.branch;

import com.bank.assets.common.response.ApiResponse;
import com.bank.assets.modules.branch.dto.BranchResponse;
import com.bank.assets.modules.branch.dto.CreateBranchRequest;
import com.bank.assets.modules.branch.dto.CreateDepartmentRequest;
import com.bank.assets.modules.branch.dto.DepartmentResponse;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@Tag(name="Branch")
@RequestMapping("branch")
@RequiredArgsConstructor
public class BranchController {
    private final BranchService branchService;

    @GetMapping("/list")
    public ResponseEntity<ApiResponse<List<BranchResponse>>> list() {
        return ResponseEntity.ok(ApiResponse.ok(branchService.listBranches()));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<BranchResponse>> create(@Valid @RequestBody CreateBranchRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.ok("Branch created.", branchService.createBranch(req)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BranchResponse>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(branchService.getBranch(id)));
    }

    @GetMapping("/{id}/dept/list")
    public ResponseEntity<ApiResponse<List<DepartmentResponse>>> getDepartments(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(branchService.listDepartments(id)));
    }

    @PostMapping("/{id}/dept")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<DepartmentResponse>> createDepartment(
        @PathVariable UUID id,
        @Valid @RequestBody CreateDepartmentRequest req
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.ok("Department created.", branchService.createDepartment(id, req)));
    }

    @GetMapping("/dept/list")
    public ResponseEntity<ApiResponse<List<DepartmentResponse>>> allDepartments() {
        return ResponseEntity.ok(ApiResponse.ok(branchService.listAllDepartments()));
    }
}
