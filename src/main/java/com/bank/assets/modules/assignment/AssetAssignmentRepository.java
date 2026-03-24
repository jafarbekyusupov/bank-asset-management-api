package com.bank.assets.modules.assignment;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AssetAssignmentRepository extends JpaRepository<AssetAssignment, UUID> {
    Optional<AssetAssignment> findByAssetIdAndReturnedAtIsNull(UUID assetId);

    boolean existsByAssetIdAndReturnedAtIsNull(UUID assetId);

    boolean existsByAssetIdAndAssignedToUserIsNotNullAndReturnedAtIsNull(UUID assetId);

    List<AssetAssignment> findByAssetIdAndReturnedAtIsNotNullAndReturnNotesIsNotNullOrderByReturnedAtDesc(UUID assetId);
}
