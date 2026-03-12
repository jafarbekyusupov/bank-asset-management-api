package com.bank.assets.modules.user;

import com.bank.assets.common.enums.UserRole;
import com.bank.assets.common.enums.UserStatus;
import com.bank.assets.common.response.ApiResponse;
import com.bank.assets.common.response.PageResponse;
import com.bank.assets.modules.user.dto.AssignDepartmentRequest;
import com.bank.assets.modules.user.dto.CreateUserRequest;
import com.bank.assets.modules.user.dto.UpdateUserStatusRequest;
import com.bank.assets.modules.user.dto.UserResponse;

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
@Tag(name="User")
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> createUser(@Valid @RequestBody CreateUserRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.ok("User created. They can now register using their email.", userService.createUser(req)));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getProfile(@AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(ApiResponse.ok(userService.getProfile(currentUser)));
    }

    @GetMapping("/list")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PageResponse<UserResponse>>> list(
        @RequestParam(required = false) UserStatus status,
        @RequestParam(required = false) UserRole role,
        @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(ApiResponse.ok(PageResponse.from(userService.list(status, role, pageable))));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(userService.getById(id)));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> changeStatus(
        @PathVariable UUID id,
        @Valid @RequestBody UpdateUserStatusRequest req,
        @AuthenticationPrincipal User currentUser
    ) {
        return ResponseEntity.ok(ApiResponse.ok("User status updated.", userService.changeStatus(id, req, currentUser)));
    }

    @PatchMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> approveUser(
        @PathVariable UUID id,
        @AuthenticationPrincipal User currentUser
    ) {
        return ResponseEntity.ok(ApiResponse.ok("User has been approved.", userService.approveUser(id, currentUser)));
    }

    @PatchMapping("/{id}/dept")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> assignDepartment(
        @PathVariable UUID id,
        @RequestBody AssignDepartmentRequest req
    ) {
        return ResponseEntity.ok(ApiResponse.ok("User department updated.", userService.assignDepartment(id, req)));
    }
}
