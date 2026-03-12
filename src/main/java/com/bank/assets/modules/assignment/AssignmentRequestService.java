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

        if (assignmentRepository.existsByAssetIdAndReturnedAtIsNull(req.assetId())) {
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
        return requestRepository.findByRequestedById(currentUser.getId(), pageable)
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

        if (assignmentRepository.existsByAssetIdAndReturnedAtIsNull(asset.getId())) {
            throw AppException.conflict(ErrorCode.ASSET_ALREADY_ASSIGNED);
        }

        User assignedToUser = request.getRequestedBy();

        AssetAssignment assignment = AssetAssignment.builder()
            .asset(asset)
            .assignedToUser(assignedToUser)
            .assignedBy(admin)
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

        historyRepository.save(AssetHistory.builder()
            .asset(request.getAsset())
            .action(AssetAction.ASSIGNMENT_REJECTED)
            .toUser(request.getRequestedBy())
            .changedBy(admin)
            .reason(req != null ? req.adminNote() : null)
            .build());
        return AssignmentRequestResponse.from(request);
    }

    private AssignmentRequest findOrThrow(UUID id) {
        return requestRepository.findById(id).orElseThrow(
            () -> AppException.notFound(ErrorCode.ASSIGNMENT_REQUEST_NOT_FOUND)
        );
    }
}
