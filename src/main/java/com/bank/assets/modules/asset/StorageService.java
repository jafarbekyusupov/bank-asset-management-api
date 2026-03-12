package com.bank.assets.modules.asset;

import com.bank.assets.common.enums.AssetAction;
import com.bank.assets.common.exception.AppException;
import com.bank.assets.common.exception.ErrorCode;
import com.bank.assets.modules.history.AssetHistory;
import com.bank.assets.modules.history.AssetHistoryRepository;
import com.bank.assets.modules.user.User;
import io.minio.*;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class StorageService {
    private final MinioClient minioClient;
    private final AssetRepository assetRepository;
    private final AssetHistoryRepository historyRepository;

    @Value("${app.minio.bucket-name}")
    private String bucket;

    @PostConstruct
    public void ensureBucket() {
        try {
            boolean exists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
            if (!exists) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
                log.info("minio bucket '{}' created.", bucket);
            }
        } catch (Exception e) {
            log.warn("could not verify/create minio bucket '{}': {}", bucket, e.getMessage());
        }
    }

    public void uploadAssetImage(UUID assetId, MultipartFile file, User uploadedBy) {
        Asset asset = assetRepository
            .findById(assetId)
            .orElseThrow(() -> AppException.notFound(ErrorCode.ASSET_NOT_FOUND));

        String objectKey = objectKey(assetId);
        String contentType = file.getContentType() != null ? file.getContentType() : "image/jpeg";

        try {
            minioClient.putObject(PutObjectArgs.builder()
                .bucket(bucket)
                .object(objectKey)
                .stream(file.getInputStream(), file.getSize(), -1)
                .contentType(contentType)
                .build());
        } catch (Exception e) {
            log.error("failed to upload image for asset {}: {}", assetId, e.getMessage());
            throw new RuntimeException("Image upload failed", e);
        }

        asset.setImageUrl(objectKey);
        assetRepository.save(asset);

        historyRepository.save(AssetHistory.builder()
            .asset(asset)
            .action(AssetAction.IMAGE_UPLOADED)
            .changedBy(uploadedBy)
            .build());
    }

    public byte[] getAssetImageBytes(UUID assetId) {
        try {
            return minioClient.getObject(GetObjectArgs.builder()
                .bucket(bucket)
                .object(objectKey(assetId))
                .build()
            ).readAllBytes();
        } catch (Exception e) {
            log.warn("image fetch failed for asset {}: {}", assetId, e.getMessage());
            return null;
        }
    }

    private String objectKey(UUID assetId) {
        return "asset-" + assetId;
    }
}
