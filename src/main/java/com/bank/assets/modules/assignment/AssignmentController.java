package com.bank.assets.modules.assignment;

import com.bank.assets.common.response.ApiResponse;
import com.bank.assets.modules.asset.dto.AssetResponse;
import com.bank.assets.modules.assignment.dto.AssignAssetRequest;
import com.bank.assets.modules.assignment.dto.AssignmentResponse;
import com.bank.assets.modules.assignment.dto.ChangeStatusRequest;
import com.bank.assets.modules.assignment.dto.ReportIssueRequest;
import com.bank.assets.modules.assignment.dto.ReturnAssetRequest;
import com.bank.assets.modules.user.User;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@Tag(name="Asset - Assignment")
@RequestMapping("/asset")
@RequiredArgsConstructor
public class AssignmentController {
    private final AssignmentService assignmentService;

    @PostMapping("/{id}/assign")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<AssignmentResponse>> assign(
        @PathVariable UUID id,
        @RequestBody AssignAssetRequest req,
        @AuthenticationPrincipal User currentUser
    ) {
        AssignmentResponse response = assignmentService.assign(id, req, currentUser);
        return ResponseEntity.ok(ApiResponse.ok("Asset assigned.", response));
    }

    @PostMapping("/{id}/return")
    public ResponseEntity<ApiResponse<AssetResponse>> returnAsset(
        @PathVariable UUID id,
        @RequestBody ReturnAssetRequest req,
        @AuthenticationPrincipal User currentUser
    ) {
        AssetResponse response = assignmentService.returnAsset(id, req, currentUser);
        return ResponseEntity.ok(ApiResponse.ok("Asset returned.", response));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<AssetResponse>> changeStatus(
        @PathVariable UUID id,
        @Valid @RequestBody ChangeStatusRequest req,
        @AuthenticationPrincipal User currentUser
    ) {
        AssetResponse response = assignmentService.changeStatus(id, req, currentUser);
        return ResponseEntity.ok(ApiResponse.ok("Status updated.", response));
    }

    @GetMapping("/{id}/assignment")
    public ResponseEntity<ApiResponse<AssignmentResponse>> getCurrentAssignment(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(assignmentService.getCurrentAssignment(id)));
    }

    @PostMapping("/{id}/report-issue")
    public ResponseEntity<ApiResponse<Void>> reportIssue(
        @PathVariable UUID id,
        @Valid @RequestBody ReportIssueRequest req,
        @AuthenticationPrincipal User currentUser
    ) {
        assignmentService.reportIssue(id, req.description(), currentUser);
        return ResponseEntity.ok(ApiResponse.ok("Issue reported."));
    }

    @PostMapping("/{id}/acknowledge")
    public ResponseEntity<ApiResponse<AssignmentResponse>> acknowledge(
        @PathVariable UUID id,
        @AuthenticationPrincipal User currentUser
    ) {
        return ResponseEntity.ok(ApiResponse.ok("Assignment acknowledged.", assignmentService.acknowledge(id, currentUser)));
    }

}
