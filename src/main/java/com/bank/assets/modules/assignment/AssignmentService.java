package com.bank.assets.modules.assignment;

import com.bank.assets.common.enums.AssetAction;
import com.bank.assets.common.enums.AssetStatus;
import com.bank.assets.common.exception.AppException;
import com.bank.assets.common.exception.ErrorCode;
import com.bank.assets.modules.asset.Asset;
import com.bank.assets.modules.asset.AssetRepository;
import com.bank.assets.modules.asset.AssetService;
import com.bank.assets.modules.asset.dto.AssetResponse;
import com.bank.assets.modules.assignment.dto.AssignAssetRequest;
import com.bank.assets.modules.assignment.dto.AssignmentResponse;
import com.bank.assets.modules.assignment.dto.ChangeStatusRequest;
import com.bank.assets.modules.assignment.dto.ReturnAssetRequest;
import com.bank.assets.modules.branch.Department;
import com.bank.assets.modules.branch.DepartmentRepository;
import com.bank.assets.modules.history.AssetHistory;
import com.bank.assets.modules.history.AssetHistoryRepository;
import com.bank.assets.modules.user.User;
import com.bank.assets.modules.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AssignmentService {
    private final AssetService assetService;
    private final AssetRepository assetRepository;
    private final AssetAssignmentRepository assignmentRepository;
    private final AssetHistoryRepository historyRepository;
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;

    @Transactional
    public AssignmentResponse assign(UUID assetId, AssignAssetRequest req, User assignedBy) {
        if (req.assignedToUserId() == null && req.assignedToDeptId() == null) {
            throw AppException.badRequest(ErrorCode.VALIDATION_ERROR);
        }

        Asset asset = assetService.findOrThrow(assetId);

        if (!asset.getStatus().canBeAssigned()) {
            throw AppException.badRequest(ErrorCode.INVALID_STATUS_TRANSITION);
        }

        if (assignmentRepository.existsByAssetIdAndReturnedAtIsNull(assetId)) {
            throw AppException.conflict(ErrorCode.ASSET_ALREADY_ASSIGNED);
        }

        User targetUser = null;
        if (req.assignedToUserId() != null) {
            targetUser = userRepository
                .findById(req.assignedToUserId())
                .orElseThrow(() -> AppException.notFound(ErrorCode.USER_NOT_FOUND));
        }

        Department targetDept = null;
        if (req.assignedToDeptId() != null) {
            targetDept = departmentRepository
                .findById(req.assignedToDeptId())
                .orElseThrow(() -> AppException.notFound(ErrorCode.DEPARTMENT_NOT_FOUND));
        }

        AssetStatus prevStatus = asset.getStatus();

        AssetAssignment assignment = AssetAssignment.builder()
            .asset(asset)
            .assignedToUser(targetUser)
            .assignedToDept(targetDept)
            .assignedBy(assignedBy)
            .notes(req.notes())
            .build();
        assignment = assignmentRepository.save(assignment);

        asset.setStatus(AssetStatus.ASSIGNED);
        asset.setOwner(targetUser);
        asset.setDepartment(targetDept);
        assetRepository.save(asset);

        historyRepository.save(AssetHistory.builder()
            .asset(asset)
            .action(AssetAction.ASSIGNED)
            .oldStatus(prevStatus)
            .newStatus(AssetStatus.ASSIGNED)
            .toUser(targetUser)
            .changedBy(assignedBy)
            .build());
        return AssignmentResponse.from(assignment);
    }

    @Transactional
    public AssetResponse returnAsset(UUID assetId, ReturnAssetRequest req, User returnedBy) {
        Asset asset = assetService.findOrThrow(assetId);

        AssetAssignment assignment = assignmentRepository
            .findByAssetIdAndReturnedAtIsNull(assetId)
            .orElseThrow(() -> AppException.badRequest(ErrorCode.ASSET_NOT_ASSIGNED));

        AssetStatus prevStatus = asset.getStatus();
        User prevOwner = assignment.getAssignedToUser();

        assignment.setReturnedAt(Instant.now());
        assignment.setReturnNotes(req.returnNotes());
        assignmentRepository.save(assignment);

        asset.setStatus(AssetStatus.REGISTERED);
        asset.setOwner(null);
        asset.setDepartment(null);
        assetRepository.save(asset);

        historyRepository.save(AssetHistory.builder()
            .asset(asset)
            .action(AssetAction.RETURNED)
            .oldStatus(prevStatus)
            .newStatus(AssetStatus.REGISTERED)
            .fromUser(prevOwner)
            .changedBy(returnedBy)
            .reason(req.returnNotes())
            .build());
        return assetService.buildResponse(asset);
    }

    @Transactional
    public AssetResponse changeStatus(UUID assetId, ChangeStatusRequest req, User changedBy) {
        Asset asset = assetService.findOrThrow(assetId);

        AssetStatus current = asset.getStatus();
        AssetStatus target = req.newStatus();

        if (!current.canTransitionTo(target)) {
            throw AppException.badRequest(ErrorCode.INVALID_STATUS_TRANSITION);
        }

        // close active assignment when asset becomes LOST or WRITTEN_OFF
        if (current == AssetStatus.ASSIGNED 
            && (target == AssetStatus.LOST || target == AssetStatus.WRITTEN_OFF)
        ) {
            Optional<AssetAssignment> active = assignmentRepository.findByAssetIdAndReturnedAtIsNull(assetId);
            active.ifPresent(a -> {
                a.setReturnedAt(Instant.now());
                a.setReturnNotes("Auto-closed: asset status changed to " + target);
                assignmentRepository.save(a);
            });
            asset.setOwner(null);
            asset.setDepartment(null);
        }

        asset.setStatus(target);
        assetRepository.save(asset);

        historyRepository.save(AssetHistory.builder()
            .asset(asset)
            .action(AssetAction.STATUS_CHANGED)
            .oldStatus(current)
            .newStatus(target)
            .changedBy(changedBy)
            .reason(req.reason())
            .build());
        return assetService.buildResponse(asset);
    }

    @Transactional(readOnly = true)
    public AssignmentResponse getCurrentAssignment(UUID assetId) {
        assetService.findOrThrow(assetId);
        return assignmentRepository.findByAssetIdAndReturnedAtIsNull(assetId)
            .map(AssignmentResponse::from)
            .orElseThrow( () -> AppException.badRequest(ErrorCode.ASSET_NOT_ASSIGNED));
    }

    @Transactional
    public void reportIssue(UUID assetId, String description, User reportedBy) {
        Asset asset = assetService.findOrThrow(assetId);
        historyRepository.save(AssetHistory.builder()
            .asset(asset)
            .action(AssetAction.ISSUE_REPORTED)
            .changedBy(reportedBy)
            .reason(description)
            .build());
    }

    @Transactional
    public AssignmentResponse acknowledge(UUID assetId, User currentUser) {
        AssetAssignment assignment = assignmentRepository
            .findByAssetIdAndReturnedAtIsNull(assetId)
            .orElseThrow( () -> AppException.badRequest(ErrorCode.ASSET_NOT_ASSIGNED));

        if (assignment.getAssignedToUser() == null 
            || !assignment.getAssignedToUser().getId().equals(currentUser.getId())
        ) {
            throw AppException.badRequest(ErrorCode.FORBIDDEN);
        }

        if (assignment.getAcknowledgedAt() != null) {
            return AssignmentResponse.from(assignment);
        }

        assignment.setAcknowledgedAt(Instant.now());
        assignment = assignmentRepository.save(assignment);

        historyRepository.save(AssetHistory.builder()
            .asset(assignment.getAsset())
            .action(AssetAction.ACKNOWLEDGED)
            .changedBy(currentUser)
            .build());
        return AssignmentResponse.from(assignment);
    }
}
