package com.bank.assets.modules.assignment;

import com.bank.assets.common.enums.AssignmentRequestStatus;
import com.bank.assets.common.response.ApiResponse;
import com.bank.assets.common.response.PageResponse;
import com.bank.assets.modules.assignment.dto.AssignmentRequestResponse;
import com.bank.assets.modules.assignment.dto.CreateAssignmentRequestRequest;
import com.bank.assets.modules.assignment.dto.ReviewAssignmentRequestRequest;
import com.bank.assets.modules.user.User;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@Tag(name="Asset - Assignment Request")
@RequestMapping("/assignment-request")
@RequiredArgsConstructor
public class AssignmentRequestController {
    private final AssignmentRequestService requestService;

    @PostMapping
    public ResponseEntity<ApiResponse<AssignmentRequestResponse>> create(
        @Valid @RequestBody CreateAssignmentRequestRequest req,
        @AuthenticationPrincipal User currentUser
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Assignment request submitted.", requestService.create(req, currentUser)));
    }

    @GetMapping("/my")
    public ResponseEntity<ApiResponse<PageResponse<AssignmentRequestResponse>>> myRequests(
        @AuthenticationPrincipal User currentUser,
        @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(ApiResponse.ok(PageResponse.from(requestService.listMyRequests(currentUser, pageable))));
    }

    @GetMapping("/list")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PageResponse<AssignmentRequestResponse>>> list(
        @RequestParam(required = false) AssignmentRequestStatus status,
        @PageableDefault(size=20, sort="createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(ApiResponse.ok(PageResponse.from(requestService.listByStatus(status, pageable))));
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<AssignmentRequestResponse>> approve(
        @PathVariable UUID id,
        @RequestBody(required = false) ReviewAssignmentRequestRequest req,
        @AuthenticationPrincipal User currentUser
    ) {
        return ResponseEntity.ok(ApiResponse.ok("Request approved.", requestService.approve(id, req, currentUser)));
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<AssignmentRequestResponse>> reject(
        @PathVariable UUID id,
        @RequestBody(required = false) ReviewAssignmentRequestRequest req,
        @AuthenticationPrincipal User currentUser
    ) {
        return ResponseEntity.ok(ApiResponse.ok("Request rejected.", requestService.reject(id, req, currentUser)));
    }
}
