package com.bank.assets.modules.asset.dto;

import com.bank.assets.common.enums.AssetStatus;
import com.bank.assets.modules.asset.Asset;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

public record AssetResponse(
        UUID id,
        String name,
        String description,
        String serialNumber,
        String brand,
        String model,
        CategoryInfo category,
        TypeInfo type,
        AssetStatus status,
        UserInfo owner,
        DeptInfo department,
        LocalDate purchaseDate,
        LocalDate warrantyUntil,
        BigDecimal purchasePrice,
        Map<String, Object> specifications,
        boolean hasImage,
        String qrCodePath,
        String notes,
        UserInfo createdBy,
        Instant createdAt,
        Instant updatedAt
) {
    public record CategoryInfo(UUID id, String name) {}
    public record TypeInfo(UUID id, String name) {}
    public record UserInfo(UUID id, String fullName, String email) {}
    public record DeptInfo(UUID id, String name) {}

    public static AssetResponse from(Asset a) {
        return new AssetResponse(
                a.getId(),
                a.getName(),
                a.getDescription(),
                a.getSerialNumber(),
                a.getBrand(),
                a.getModel(),
                a.getCategory() != null
                        ? new CategoryInfo(a.getCategory().getId(), a.getCategory().getName()) 
                        : null,
                a.getType() != null
                        ? new TypeInfo(a.getType().getId(), a.getType().getName()) 
                        : null,
                a.getStatus(),
                a.getOwner() != null
                        ? new UserInfo(a.getOwner().getId(), a.getOwner().getFullName(), a.getOwner().getEmail()) 
                        : null,
                a.getDepartment() != null
                        ? new DeptInfo(a.getDepartment().getId(), a.getDepartment().getName()) 
                        : null,
                a.getPurchaseDate(),
                a.getWarrantyUntil(),
                a.getPurchasePrice(),
                a.getSpecifications(),
                a.getImageUrl() != null,
                "/pub/asset/" + a.getId() + "/qr",
                a.getNotes(),
                a.getCreatedBy() != null
                        ? new UserInfo(a.getCreatedBy().getId(), a.getCreatedBy().getFullName(), a.getCreatedBy().getEmail()) 
                        : null,
                a.getCreatedAt(),
                a.getUpdatedAt()
        );
    }
}
