package com.bank.assets.modules.branch;

import com.bank.assets.common.response.ApiResponse;
import com.bank.assets.modules.branch.dto.BranchResponse;
import com.bank.assets.modules.branch.dto.CreateBranchRequest;
import com.bank.assets.modules.branch.dto.CreateDepartmentRequest;
import com.bank.assets.modules.branch.dto.DepartmentResponse;
import com.bank.assets.modules.branch.dto.UpdateBranchRequest;
import com.bank.assets.modules.branch.dto.UpdateDepartmentRequest;

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

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<BranchResponse>> update(
        @PathVariable UUID id,
        @Valid @RequestBody UpdateBranchRequest req
    ) {
        return ResponseEntity.ok(ApiResponse.ok("Branch updated.", branchService.updateBranch(id, req)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> archive(@PathVariable UUID id) {
        branchService.archiveBranch(id);
        return ResponseEntity.ok(ApiResponse.ok("Branch archived.", null));
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

    @GetMapping("/dept/{id}")
    public ResponseEntity<ApiResponse<DepartmentResponse>> getDepartmentById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(branchService.getDepartment(id)));
    }

    @PutMapping("/dept/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<DepartmentResponse>> updateDepartment(
        @PathVariable UUID id,
        @Valid @RequestBody UpdateDepartmentRequest req
    ) {
        return ResponseEntity.ok(ApiResponse.ok("Department updated.", branchService.updateDepartment(id, req)));
    }

    @DeleteMapping("/dept/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> archiveDepartment(@PathVariable UUID id) {
        branchService.archiveDepartment(id);
        return ResponseEntity.ok(ApiResponse.ok("Department archived.", null));
    }
}
