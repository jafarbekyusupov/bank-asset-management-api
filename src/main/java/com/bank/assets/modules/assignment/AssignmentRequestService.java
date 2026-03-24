package com.bank.assets.modules.assignment;

import com.bank.assets.common.enums.AssetAction;
import com.bank.assets.common.enums.AssetStatus;
import com.bank.assets.common.enums.AssignmentRequestStatus;
import com.bank.assets.common.exception.AppException;
import com.bank.assets.common.exception.ErrorCode;
import com.bank.assets.modules.asset.Asset;
import com.bank.assets.modules.asset.AssetRepository;
import com.bank.assets.modules.asset.AssetService;
import com.bank.assets.modules.assignment.dto.AssignmentRequestResponse;
import com.bank.assets.modules.assignment.dto.CreateAssignmentRequestRequest;
import com.bank.assets.modules.assignment.dto.ReviewAssignmentRequestRequest;
import com.bank.assets.modules.history.AssetHistory;
import com.bank.assets.modules.history.AssetHistoryRepository;
import com.bank.assets.modules.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AssignmentRequestService {
    private final AssignmentRequestRepository requestRepository;
    private final AssetAssignmentRepository assignmentRepository;
    private final AssetService assetService;
    private final AssetRepository assetRepository;
    private final AssetHistoryRepository historyRepository;

    @Transactional
    public AssignmentRequestResponse create(CreateAssignmentRequestRequest req, User requestedBy) {
        Asset asset = assetService.findOrThrow(req.assetId());

        if (!asset.getStatus().canBeAssigned()) {
            throw AppException.badRequest(ErrorCode.INVALID_STATUS_TRANSITION);
        }

        validateAssetInScope(asset, requestedBy);

        if (assignmentRepository.existsByAssetIdAndAssignedToUserIsNotNullAndReturnedAtIsNull(req.assetId())) {
            throw AppException.conflict(ErrorCode.ASSET_ALREADY_ASSIGNED);
        }

        if (requestRepository.existsByAssetIdAndRequestedByIdAndStatus(
                req.assetId(), requestedBy.getId(), AssignmentRequestStatus.PENDING)) {
            throw AppException.conflict(ErrorCode.ASSIGNMENT_REQUEST_ALREADY_PENDING);
        }

        AssignmentRequest request = AssignmentRequest.builder()
            .asset(asset)
            .requestedBy(requestedBy)
            .reason(req.reason())
            .build();

        request = requestRepository.save(request);

        historyRepository.save(AssetHistory.builder()
            .asset(asset)
            .action(AssetAction.ASSIGNMENT_REQUESTED)
            .toUser(requestedBy)
            .toDept(requestedBy.getDepartment())
            .toBranch(requestedBy.getBranch())
            .changedBy(requestedBy)
            .reason(req.reason())
            .build());
        return AssignmentRequestResponse.from(request);
    }

    @Transactional(readOnly = true)
    public Page<AssignmentRequestResponse> listByStatus(AssignmentRequestStatus status, Pageable pageable) {
        return status != null
            ? requestRepository.findByStatus(status, pageable).map(AssignmentRequestResponse::from)
            : requestRepository.findAll(pageable).map(AssignmentRequestResponse::from);
    }

    @Transactional(readOnly = true)
    public Page<AssignmentRequestResponse> listMyRequests(User currentUser, Pageable pageable) {
        return requestRepository
            .findByRequestedById(currentUser.getId(), pageable)
            .map(AssignmentRequestResponse::from);
    }

    @Transactional
    public AssignmentRequestResponse approve(UUID requestId, ReviewAssignmentRequestRequest req, User admin) {
        AssignmentRequest request = findOrThrow(requestId);

        if (request.getStatus() != AssignmentRequestStatus.PENDING) {
            throw AppException.badRequest(ErrorCode.ASSIGNMENT_REQUEST_NOT_PENDING);
        }

        Asset asset = request.getAsset();

        if (!asset.getStatus().canBeAssigned()) {
            throw AppException.badRequest(ErrorCode.INVALID_STATUS_TRANSITION);
        }

        if (assignmentRepository.existsByAssetIdAndAssignedToUserIsNotNullAndReturnedAtIsNull(asset.getId())) {
            throw AppException.conflict(ErrorCode.ASSET_ALREADY_ASSIGNED);
        }

        // close any open dept/branch lvl assignment before assigning to user
        assignmentRepository.findByAssetIdAndReturnedAtIsNull(asset.getId()).ifPresent(existing -> {
            existing.setReturnedAt(Instant.now());
            existing.setReturnNotes("Auto-closed: asset assigned to user via request approval");
            assignmentRepository.saveAndFlush(existing);
        });

        User assignedToUser = request.getRequestedBy();

        AssetAssignment assignment = AssetAssignment.builder()
            .asset(asset)
            .assignedToUser(assignedToUser)
            .assignedBy(admin)
            .assignedAt(Instant.now())
            .notes(request.getReason())
            .build();
        assignmentRepository.save(assignment);

        AssetStatus prevStatus = asset.getStatus();
        asset.setStatus(AssetStatus.ASSIGNED);
        asset.setOwner(assignedToUser);
        assetRepository.save(asset);

        request.setStatus(AssignmentRequestStatus.APPROVED);
        request.setReviewedBy(admin);
        request.setAdminNote(req != null ? req.adminNote() : null);
        request.setReviewedAt(Instant.now());
        request = requestRepository.save(request);

        historyRepository.save(AssetHistory.builder()
            .asset(asset)
            .action(AssetAction.ASSIGNMENT_APPROVED)
            .oldStatus(prevStatus)
            .newStatus(AssetStatus.ASSIGNED)
            .toUser(assignedToUser)
            .toDept(assignedToUser.getDepartment())
            .toBranch(assignedToUser.getBranch())
            .changedBy(admin)
            .build());
        return AssignmentRequestResponse.from(request);
    }

    @Transactional
    public AssignmentRequestResponse reject(UUID requestId, ReviewAssignmentRequestRequest req, User admin) {
        AssignmentRequest request = findOrThrow(requestId);

        if (request.getStatus() != AssignmentRequestStatus.PENDING) {
            throw AppException.badRequest(ErrorCode.ASSIGNMENT_REQUEST_NOT_PENDING);
        }

        request.setStatus(AssignmentRequestStatus.REJECTED);
        request.setReviewedBy(admin);
        request.setAdminNote(req != null ? req.adminNote() : null);
        request.setReviewedAt(Instant.now());
        request = requestRepository.save(request);

        User rejectedUser = request.getRequestedBy();
        historyRepository.save(AssetHistory.builder()
            .asset(request.getAsset())
            .action(AssetAction.ASSIGNMENT_REJECTED)
            .toUser(rejectedUser)
            .toDept(rejectedUser.getDepartment())
            .toBranch(rejectedUser.getBranch())
            .changedBy(admin)
            .reason(req != null ? req.adminNote() : null)
            .build());
        return AssignmentRequestResponse.from(request);
    }

    private void validateAssetInScope(Asset asset, User user) {
        switch (user.getRole()) {
            case STAFF -> {
                if (user.getDepartment() == null
                    || asset.getDepartment() == null
                    || !asset.getDepartment().getId().equals(user.getDepartment().getId())
                ) {
                    throw AppException.badRequest(ErrorCode.FORBIDDEN);
                }
            }
            case DEPT_MANAGER -> {
                if (user.getBranch() == null) throw AppException.badRequest(ErrorCode.FORBIDDEN);
                boolean viaDept = asset.getDepartment() != null
                    && asset.getDepartment().getBranch().getId().equals(user.getBranch().getId());
                boolean viaBranch = asset.getBranch() != null
                    && asset.getBranch().getId().equals(user.getBranch().getId());
                if (!viaDept && !viaBranch) throw AppException.badRequest(ErrorCode.FORBIDDEN);
            }
            default -> {} // branch_mngr and admin - no scope restrictions
        }
    }

    private AssignmentRequest findOrThrow(UUID id) {
        return requestRepository.findById(id).orElseThrow(
            () -> AppException.notFound(ErrorCode.ASSIGNMENT_REQUEST_NOT_FOUND)
        );
    }
}
