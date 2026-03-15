package com.bank.assets.modules.asset;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface AssetTypeRepository extends JpaRepository<AssetType, UUID> {
    List<AssetType> findByCategoryId(UUID categoryId);

    boolean existsByNameIgnoreCaseAndCategoryId(String name, UUID categoryId);

    boolean existsByCategoryId(UUID categoryId);

    @Query("SELECT t FROM AssetType t JOIN FETCH t.category ORDER BY t.name")
    List<AssetType> findAllWithCategory();

    @Query("SELECT t FROM AssetType t JOIN FETCH t.category WHERE t.category.id = :categoryId ORDER BY t.name")
    List<AssetType> findByCategoryIdWithCategory(@Param("categoryId") UUID categoryId);

    @Query("SELECT t.category.name as categoryName, COUNT(t) as count FROM AssetType t GROUP BY t.category.name")
    List<TypeCountByCategory> countTypesByCategory();

    interface TypeCountByCategory {
        String getCategoryName();
        long getCount();
    }
}
