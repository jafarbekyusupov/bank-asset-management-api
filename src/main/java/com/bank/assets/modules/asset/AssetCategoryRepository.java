package com.bank.assets.modules.asset;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface AssetCategoryRepository extends JpaRepository<AssetCategory, UUID> {
    Optional<AssetCategory> findByName(String name);
}
