package com.bank.assets.modules.asset;

import com.bank.assets.common.enums.AssetStatus;
import org.springframework.data.jpa.domain.Specification;

import java.util.UUID;

public class AssetSpecification {
    private AssetSpecification() {}

    public static Specification<Asset> withFilters(
        AssetStatus status,
        UUID categoryId,
        UUID typeId,
        UUID ownerId,
        UUID deptId,
        String search
    ) {
        return Specification
            .where(hasStatus(status))
            .and(hasCategory(categoryId))
            .and(hasType(typeId))
            .and(hasOwner(ownerId))
            .and(hasDept(deptId))
            .and(matchesSearch(search));
    }

    private static Specification<Asset> hasStatus(AssetStatus status) {
        return status == null ? null : (root, query, cb) -> cb.equal(root.get("status"), status);
    }

    private static Specification<Asset> hasCategory(UUID categoryId) {
        return categoryId == null 
            ? null
            : (root, query, cb) -> cb.equal(root.get("category").get("id"), categoryId);
    }

    private static Specification<Asset> hasType(UUID typeId) {
        return typeId == null 
            ? null
            : (root, query, cb) -> cb.equal(root.get("type").get("id"), typeId);
    }

    private static Specification<Asset> hasOwner(UUID ownerId) {
        return ownerId == null 
            ? null
            : (root, query, cb) -> cb.equal(root.get("owner").get("id"), ownerId);
    }

    private static Specification<Asset> hasDept(UUID deptId) {
        return deptId == null 
            ? null 
            : (root, q, cb) -> cb.equal(root.get("department").get("id"), deptId);
    }

    private static Specification<Asset> matchesSearch(String search) {
        if (search == null || search.isBlank()) return null;
        String pattern = "%" + search.toLowerCase() + "%";
        return (root, query, cb) -> cb.or(
            cb.like(cb.lower(root.get("name")), pattern),
            cb.like(cb.lower(root.get("serialNumber")), pattern),
            cb.like(cb.lower(root.get("brand")), pattern),
            cb.like(cb.lower(root.get("model")), pattern)
        );
    }
}
