package com.bank.assets.modules.assignment;

import com.bank.assets.common.enums.AssignmentRequestStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AssignmentRequestRepository extends JpaRepository<AssignmentRequest, UUID> {

    Page<AssignmentRequest> findByStatus(AssignmentRequestStatus status, Pageable pageable);

    Page<AssignmentRequest> findByRequestedById(UUID userId, Pageable pageable);

    boolean existsByAssetIdAndRequestedByIdAndStatus(UUID assetId, UUID userId, AssignmentRequestStatus status);
}
