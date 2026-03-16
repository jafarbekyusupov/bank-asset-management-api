package com.bank.assets.modules.asset;

import com.bank.assets.common.enums.AssetStatus;
import com.bank.assets.common.exception.AppException;
import com.bank.assets.common.exception.ErrorCode;
import com.bank.assets.common.response.ApiResponse;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@Tag(name="Asset - Public")
@RequestMapping("/pub/asset")
@RequiredArgsConstructor
public class PublicAssetController {
    private final AssetRepository assetRepository;
    private final QrService qrService;

    public record PublicAssetInfo(
        UUID id,
        String name,
        String serialNumber,
        String brand,
        String model,
        AssetStatus status,
        String categoryName,
        String typeName,
        String ownerName,
        String departmentName,
        String branchName,
        boolean hasImage
    ) {}

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PublicAssetInfo>> getPublicInfo(@PathVariable UUID id) {
        Asset asset = assetRepository.findById(id)
            .orElseThrow(() -> AppException.notFound(ErrorCode.ASSET_NOT_FOUND));
        PublicAssetInfo info = new PublicAssetInfo(
            asset.getId(),
            asset.getName(),
            asset.getSerialNumber(),
            asset.getBrand(),
            asset.getModel(),
            asset.getStatus(),
            asset.getCategory() != null ? asset.getCategory().getName() : null,
            asset.getType() != null ? asset.getType().getName() : null,
            asset.getOwner() != null ? asset.getOwner().getFullName() : null,
            asset.getDepartment() != null ? asset.getDepartment().getName() : null,
            asset.getBranch() != null ? asset.getBranch().getName() : null,
            asset.getImageUrl() != null
        );
        return ResponseEntity.ok(ApiResponse.ok(info));
    }

    @GetMapping(value = "/{id}/qr", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> getQrCode(@PathVariable UUID id) {
        if (!assetRepository.existsById(id)) {
            throw AppException.notFound(ErrorCode.ASSET_NOT_FOUND);
        }
        byte[] png = qrService.generateAssetQr(id);
        return ResponseEntity.ok()
            .contentType(MediaType.IMAGE_PNG)
            .body(png);
    }
}
