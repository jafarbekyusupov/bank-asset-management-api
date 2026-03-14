package com.bank.assets.modules.history;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface AssetHistoryRepository extends JpaRepository<AssetHistory, UUID> {
    Page<AssetHistory> findByAssetIdOrderByChangedAtDesc(UUID assetId, Pageable pageable);

    @Query("""
        SELECT h FROM AssetHistory h
        WHERE h.toUser.id = :userId
           OR h.fromUser.id = :userId
           OR h.changedBy.id = :userId
        ORDER BY h.changedAt DESC
        """)
    Page<AssetHistory> findActivityForUser(@Param("userId") UUID userId, Pageable pageable);

    @Query("""
        SELECT h FROM AssetHistory h
        WHERE h.toDept.id = :deptId
           OR h.fromDept.id = :deptId
        ORDER BY h.changedAt DESC
        """)
    Page<AssetHistory> findActivityForDept(@Param("deptId") UUID deptId, Pageable pageable);

    @Query("""
        SELECT h FROM AssetHistory h
        WHERE h.toBranch.id = :branchId
           OR h.fromBranch.id = :branchId
        ORDER BY h.changedAt DESC
        """)
    Page<AssetHistory> findActivityForBranch(@Param("branchId") UUID branchId, Pageable pageable);
}
