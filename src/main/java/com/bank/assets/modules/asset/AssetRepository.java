package com.bank.assets.modules.asset;

import com.bank.assets.common.enums.AssetStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AssetRepository extends JpaRepository<Asset, UUID>, JpaSpecificationExecutor<Asset> {
    boolean existsBySerialNumber(String serialNumber);

    Optional<Asset> findBySerialNumber(String serialNumber);

    long countByStatus(AssetStatus status);

    @Query("SELECT a.status as status, COUNT(a) as count FROM Asset a GROUP BY a.status")
    List<StatusCount> countGroupedByStatus();

    @Query("SELECT c.name as categoryName, COUNT(a) as count " +
           "FROM Asset a JOIN a.category c GROUP BY c.name ORDER BY count DESC")
    List<CategoryCount> countGroupedByCategory();

    @Query("SELECT d.name as deptName, COUNT(a) as count " +
           "FROM Asset a JOIN a.department d GROUP BY d.name ORDER BY count DESC")
    List<DeptCount> countGroupedByDepartment();

    @Query("SELECT COUNT(a) FROM Asset a " +
           "WHERE a.warrantyUntil < :now AND a.status NOT IN :excluded")
    long countWithExpiredWarranty(
       @Param("now") LocalDate now,
       @Param("excluded") List<AssetStatus> excluded);

    @Query("SELECT COUNT(a) FROM Asset a " +
           "WHERE a.warrantyUntil BETWEEN :from AND :to AND a.status NOT IN :excluded")
    long countWithWarrantyExpiringSoon(
       @Param("from") LocalDate from,
       @Param("to") LocalDate to,
       @Param("excluded") List<AssetStatus> excluded);

    @Query("SELECT a FROM Asset a WHERE a.warrantyUntil <= :threshold " +
           "AND a.status NOT IN :excluded ORDER BY a.warrantyUntil ASC")
    List<Asset> findWithWarrantyUpTo(
       @Param("threshold") LocalDate threshold,
       @Param("excluded") List<AssetStatus> excluded);

    interface StatusCount {
        AssetStatus getStatus();
        long getCount();
    }

    interface CategoryCount {
        String getCategoryName();
        long getCount();
    }

    @Query("SELECT c.name as categoryName, c.description as description, a.status as status, COUNT(a) as count " +
           "FROM Asset a JOIN a.category c GROUP BY c.name, c.description, a.status")
    List<CategoryStatusCount> countGroupedByCategoryAndStatus();

    interface CategoryStatusCount {
        String getCategoryName();
        String getDescription();
        AssetStatus getStatus();
        long getCount();
    }

    interface DeptCount {
        String getDeptName();
        long getCount();
    }
}
