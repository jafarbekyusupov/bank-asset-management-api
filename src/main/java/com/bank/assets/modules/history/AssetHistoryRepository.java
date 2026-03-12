package com.bank.assets.modules.history;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AssetHistoryRepository extends JpaRepository<AssetHistory, UUID> {
    Page<AssetHistory> findByAssetIdOrderByChangedAtDesc(UUID assetId, Pageable pageable);
}
