package com.bank.assets.modules.user;

import com.bank.assets.common.enums.UserRole;
import com.bank.assets.common.enums.UserStatus;
import com.bank.assets.common.exception.AppException;
import com.bank.assets.common.exception.ErrorCode;
import com.bank.assets.modules.asset.AssetRepository;
import com.bank.assets.modules.branch.Branch;
import com.bank.assets.modules.branch.BranchRepository;
import com.bank.assets.modules.branch.Department;
import com.bank.assets.modules.branch.DepartmentRepository;
import com.bank.assets.modules.user.dto.AssignDepartmentRequest;
import com.bank.assets.modules.user.dto.CreateUserRequest;
import com.bank.assets.modules.user.dto.UpdateRoleRequest;
import com.bank.assets.modules.user.dto.UpdateUserRequest;
import com.bank.assets.modules.user.dto.UpdateUserStatusRequest;
import com.bank.assets.modules.user.dto.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final BranchRepository branchRepository;
    private final AssetRepository assetRepository;

    @Transactional
    public UserResponse createUser(CreateUserRequest req) {
        if (userRepository.existsByEmail(req.email())) {
            throw AppException.conflict(ErrorCode.USER_ALREADY_EXISTS);
        }

        Department dept = null;
        Branch branch = null;

        if (req.deptId() != null) {
            dept = departmentRepository
                .findById(req.deptId())
                .orElseThrow(() -> AppException.notFound(ErrorCode.DEPARTMENT_NOT_FOUND));
            branch = dept.getBranch();
        } else if (req.branchId() != null) {
            branch = branchRepository
                .findById(req.branchId())
                .orElseThrow(() -> AppException.notFound(ErrorCode.BRANCH_NOT_FOUND));
        }

        User user = User.builder()
            .fullName(req.fullName())
            .email(req.email())
            .role(req.role())
            .status(UserStatus.PENDING)
            .isDev(Boolean.TRUE.equals(req.isDev()))
            .department(dept)
            .branch(branch)
            .build();
        return UserResponse.from(userRepository.save(user));
    }

    @Transactional(readOnly = true)
    public Page<UserResponse> list(
        UserStatus status, 
        UserRole role, 
        UUID deptId, 
        UUID branchId, 
        String search, 
        Pageable pageable
    ) {
        return userRepository
            .findAll(UserSpecification.withFilters(status, role, deptId, branchId, search), pageable)
            .map(UserResponse::from);
    }

    @Transactional(readOnly = true)
    public UserResponse getById(UUID id) {
        User user = findOrThrow(id);
        return UserResponse.from(user, !assetRepository.existsByOwnerId(id));
    }

    @Transactional(readOnly = true)
    public UserResponse getProfile(User currentUser) {
        return UserResponse.from(findOrThrow(currentUser.getId()));
    }

    @Transactional
    public UserResponse changeStatus(UUID targetId, UpdateUserStatusRequest req, User admin) {
        if (targetId.equals(admin.getId())) {
            throw AppException.badRequest(ErrorCode.FORBIDDEN);
        }

        User target = findOrThrow(targetId);
        target.setStatus(req.newStatus());
        return UserResponse.from(userRepository.save(target));
    }

    @Transactional
    public UserResponse approveUser(UUID targetId, User admin) {
        if (targetId.equals(admin.getId())) {
            throw AppException.badRequest(ErrorCode.FORBIDDEN);
        }
        User target = findOrThrow(targetId);
        target.setStatus(UserStatus.ACTIVE);
        return UserResponse.from(userRepository.save(target));
    }

    @Transactional
    public UserResponse assignDepartment(UUID userId, AssignDepartmentRequest req) {
        User user = findOrThrow(userId);

        if (req.departmentId() != null) {
            Department dept = departmentRepository
                .findById(req.departmentId())
                .orElseThrow(() -> AppException.notFound(ErrorCode.DEPARTMENT_NOT_FOUND));
            user.setDepartment(dept);
            user.setBranch(dept.getBranch());
        } else {
            user.setDepartment(null);
        }

        if (req.branchId() != null && req.departmentId() == null) {
            Branch branch = branchRepository
                .findById(req.branchId())
                .orElseThrow(() -> AppException.notFound(ErrorCode.BRANCH_NOT_FOUND));
            user.setBranch(branch);
        } else if (req.branchId() == null && req.departmentId() == null) {
            user.setBranch(null);
        }
        return UserResponse.from(userRepository.save(user));
    }

    @Transactional
    public UserResponse updateUser(UUID targetId, UpdateUserRequest req, User admin) {
        if (targetId.equals(admin.getId())) {
            throw AppException.badRequest(ErrorCode.FORBIDDEN);
        }
        User user = findOrThrow(targetId);

        if (!user.getEmail().equals(req.email()) && userRepository.existsByEmail(req.email())) {
            throw AppException.conflict(ErrorCode.USER_ALREADY_EXISTS);
        }

        user.setFullName(req.fullName());
        user.setEmail(req.email());

        if (req.deptId() != null) {
            Department dept = departmentRepository
                .findById(req.deptId())
                .orElseThrow(() -> AppException.notFound(ErrorCode.DEPARTMENT_NOT_FOUND));
            user.setDepartment(dept);
            user.setBranch(dept.getBranch());
        } else if (req.branchId() != null) {
            user.setDepartment(null);
            Branch branch = branchRepository
                .findById(req.branchId())
                .orElseThrow(() -> AppException.notFound(ErrorCode.BRANCH_NOT_FOUND));
            user.setBranch(branch);
        } else {
            user.setDepartment(null);
            user.setBranch(null);
        }

        return UserResponse.from(userRepository.save(user));
    }

    @Transactional
    public UserResponse updateRole(UUID targetId, UpdateRoleRequest req, User admin) {
        if (targetId.equals(admin.getId())) {
            throw AppException.badRequest(ErrorCode.FORBIDDEN);
        }
        User user = findOrThrow(targetId);
        user.setRole(req.role());
        return UserResponse.from(userRepository.save(user));
    }

    @Transactional
    public void deleteUser(UUID targetId, User admin) {
        if (targetId.equals(admin.getId())) {
            throw AppException.badRequest(ErrorCode.FORBIDDEN);
        }
        User user = findOrThrow(targetId);
        if (assetRepository.existsByOwnerId(targetId)) {
            throw AppException.conflict(ErrorCode.USER_HAS_ASSETS);
        }
        userRepository.delete(user);
    }

    private User findOrThrow(UUID id) {
        return userRepository
            .findById(id)
            .orElseThrow(() -> AppException.notFound(ErrorCode.USER_NOT_FOUND));
    }
}
