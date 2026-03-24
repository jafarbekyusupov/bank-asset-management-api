package com.bank.assets.modules.asset;

import com.bank.assets.common.enums.AssetAction;
import com.bank.assets.common.enums.AssetStatus;
import com.bank.assets.common.exception.AppException;
import com.bank.assets.common.exception.ErrorCode;
import com.bank.assets.modules.asset.dto.AssetNoteResponse;
import com.bank.assets.modules.asset.dto.AssetResponse;
import com.bank.assets.modules.asset.dto.CreateAssetRequest;
import com.bank.assets.modules.asset.dto.UpdateAssetRequest;
import com.bank.assets.modules.assignment.AssetAssignmentRepository;
import com.bank.assets.modules.history.AssetHistory;
import com.bank.assets.modules.history.AssetHistoryRepository;
import com.bank.assets.modules.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AssetService {
    private final AssetRepository assetRepository;
    private final AssetCategoryRepository categoryRepository;
    private final AssetTypeRepository typeRepository;
    private final AssetHistoryRepository historyRepository;
    private final AssetAssignmentRepository assignmentRepository;

    @Transactional
    public AssetResponse create(CreateAssetRequest req, User createdBy) {
        if (assetRepository.existsBySerialNumber(req.serialNumber())) {
            throw AppException.conflict(ErrorCode.ASSET_SERIAL_EXISTS);
        }

        AssetCategory category = categoryRepository
            .findById(req.categoryId())
            .orElseThrow(() -> AppException.notFound(ErrorCode.CATEGORY_NOT_FOUND));

        AssetType type = typeRepository
            .findById(req.typeId())
            .orElseThrow(() -> AppException.notFound(ErrorCode.TYPE_NOT_FOUND));

        Asset asset = Asset.builder()
            .name(req.name())
            .description(req.description())
            .serialNumber(req.serialNumber())
            .brand(req.brand())
            .model(req.model())
            .category(category)
            .type(type)
            .purchaseDate(req.purchaseDate())
            .warrantyUntil(req.warrantyUntil())
            .purchasePrice(req.purchasePrice())
            .specifications(req.specifications() != null ? req.specifications() : new HashMap<>())
            .notes(req.notes())
            .createdBy(createdBy)
            .build();

        asset = assetRepository.save(asset);

        historyRepository.save(AssetHistory.builder()
            .asset(asset)
            .action(AssetAction.CREATED)
            .newStatus(AssetStatus.REGISTERED)
            .changedBy(createdBy)
            .build());
        return buildResponse(asset);
    }

    @Transactional(readOnly = true)
    public Page<AssetResponse> list(
        AssetStatus status,
        UUID categoryId,
        UUID typeId,
        UUID ownerId,
        UUID deptId,
        String search,
        User currentUser,
        Pageable pageable
    ) {
        Specification<Asset> spec = AssetSpecification.withFilters(status, categoryId, typeId, ownerId, deptId, search)
                .and(AssetSpecification.scopedFor(currentUser));
        return assetRepository.findAll(spec, pageable).map(this::buildResponse);
    }

    @Transactional(readOnly = true)
    public AssetResponse getById(UUID id) {
        return buildResponse(findOrThrow(id));
    }

    @Transactional(readOnly = true)
    public AssetResponse getBySerialNumber(String serialNumber) {
        return buildResponse(assetRepository.findBySerialNumber(serialNumber)
            .orElseThrow(() -> AppException.notFound(ErrorCode.ASSET_NOT_FOUND)));
    }

    @Transactional
    public AssetResponse update(UUID id, UpdateAssetRequest req, User updatedBy) {
        Asset asset = findOrThrow(id);
        if (req.name() != null) asset.setName(req.name());
        if (req.description() != null)  asset.setDescription(req.description());
        if (req.brand() != null)    asset.setBrand(req.brand());
        if (req.model() != null)    asset.setModel(req.model());
        if (req.purchaseDate() != null) asset.setPurchaseDate(req.purchaseDate());
        if (req.warrantyUntil() != null)    asset.setWarrantyUntil(req.warrantyUntil());
        if (req.purchasePrice() != null)    asset.setPurchasePrice(req.purchasePrice());
        if (req.specifications() != null)   asset.setSpecifications(req.specifications());
        if (req.notes() != null)    asset.setNotes(req.notes());

        if (req.typeId() != null) {
            AssetType type = typeRepository
                .findById(req.typeId())
                .orElseThrow(() -> AppException.notFound(ErrorCode.TYPE_NOT_FOUND));
            asset.setType(type);
        }

        asset = assetRepository.save(asset);
        historyRepository.save(AssetHistory.builder()
            .asset(asset)
            .action(AssetAction.UPDATED)
            .changedBy(updatedBy)
            .build());
        return buildResponse(asset);
    }

    @Transactional
    public void delete(UUID id) {
        if (!assetRepository.existsById(id)) {
            throw AppException.notFound(ErrorCode.ASSET_NOT_FOUND);
        }
        assetRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public Page<AssetResponse> listAssignable(User currentUser, Pageable pageable) {
        Specification<Asset> spec = AssetSpecification.assignableScopedFor(currentUser);
        return assetRepository.findAll(spec, pageable).map(this::buildResponse);
    }

    @Transactional(readOnly = true)
    public List<AssetNoteResponse> getNotes(UUID assetId) {
        if (!assetRepository.existsById(assetId)) {
            throw AppException.notFound(ErrorCode.ASSET_NOT_FOUND);
        }
        List<AssetNoteResponse> notes = new ArrayList<>();
        historyRepository.findByAssetIdAndReasonIsNotNullAndSystemNoteIsFalseOrderByChangedAtDesc(assetId)
                .stream().map(AssetNoteResponse::fromHistory).forEach(notes::add);
        assignmentRepository.findByAssetIdAndReturnedAtIsNotNullAndReturnNotesIsNotNullAndSystemNoteIsFalseOrderByReturnedAtDesc(assetId)
                .stream().map(AssetNoteResponse::fromAssignment).forEach(notes::add);
        notes.sort(Comparator.comparing(AssetNoteResponse::date).reversed());
        return notes;
    }

    public AssetResponse buildResponse(Asset asset) {
        return AssetResponse.from(asset);
    }

    public Asset findOrThrow(UUID id) {
        return assetRepository.findById(id).orElseThrow(() -> AppException.notFound(ErrorCode.ASSET_NOT_FOUND));
    }
}
