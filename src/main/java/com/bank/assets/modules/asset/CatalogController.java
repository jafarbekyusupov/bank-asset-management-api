package com.bank.assets.modules.asset;

import com.bank.assets.common.exception.AppException;
import com.bank.assets.common.exception.ErrorCode;
import com.bank.assets.common.response.ApiResponse;
import com.bank.assets.modules.asset.dto.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@Tag(name="Catalog")
@RequestMapping("/catalog")
@RequiredArgsConstructor
public class CatalogController {
    private final AssetCategoryRepository categoryRepo;
    private final AssetTypeRepository typeRepo;

    @GetMapping("/category/list")
    public ApiResponse<List<CategoryResponse>> listCategories() {
        return ApiResponse.ok(
                categoryRepo.findAll(Sort.by("name"))
                        .stream()
                        .map(c -> new CategoryResponse(c.getId(), c.getName(), c.getDescription()))
                        .toList()
        );
    }

    @PostMapping("/category")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<CategoryResponse> createCategory(@Valid @RequestBody CreateCategoryRequest req) {
        if (categoryRepo.findByName(req.name()).isPresent()) {
            throw AppException.conflict(ErrorCode.CATEGORY_ALREADY_EXISTS);
        }
        AssetCategory saved = categoryRepo.save(
                AssetCategory.builder()
                        .name(req.name())
                        .description(req.description())
                        .build()
        );
        return ApiResponse.ok(new CategoryResponse(saved.getId(), saved.getName(), saved.getDescription()));
    }
    
    @GetMapping("/type/list")
    public ApiResponse<List<AssetTypeResponse>> listTypes(
        @RequestParam(required = false) UUID categoryId) {
                List<AssetType> types = categoryId != null
                        ? typeRepo.findByCategoryIdWithCategory(categoryId)
                        : typeRepo.findAllWithCategory();

                return ApiResponse.ok(types.stream().map(t -> new AssetTypeResponse(
                        t.getId(),
                        t.getName(),
                        t.getDescription(),
                        t.getCategory().getId(),
                        t.getCategory().getName()
                )).toList());
        }

    @PostMapping("/type")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<AssetTypeResponse> createType(@Valid @RequestBody CreateAssetTypeRequest req) {
        AssetCategory category = categoryRepo.findById(req.categoryId()).orElseThrow(
                () -> AppException.notFound(ErrorCode.CATEGORY_NOT_FOUND)
        );

        if (typeRepo.existsByNameIgnoreCaseAndCategoryId(req.name(), req.categoryId())) {
            throw AppException.conflict(ErrorCode.TYPE_ALREADY_EXISTS);
        }

        AssetType saved = typeRepo.save(
                AssetType.builder()
                        .name(req.name())
                        .description(req.description())
                        .category(category)
                        .build()
        );
        return ApiResponse.ok(new AssetTypeResponse(
                saved.getId(),
                saved.getName(),
                saved.getDescription(),
                category.getId(),
                category.getName()));
    }

    @PostMapping("/type/bulk")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<BulkCreateTypeResult>> createTypesBulk(
            @Valid @RequestBody BulkCreateAssetTypeRequest req) {

        AssetCategory category = categoryRepo.findById(req.categoryId()).orElseThrow(
                () -> AppException.notFound(ErrorCode.CATEGORY_NOT_FOUND)
        );

        List<String> skipped = req.types().stream()
                .filter(e -> typeRepo.existsByNameIgnoreCaseAndCategoryId(e.name(), req.categoryId()))
                .map(BulkCreateAssetTypeRequest.TypeEntry::name)
                .toList();

        List<AssetType> toSave = req.types().stream()
                .filter(e -> !typeRepo.existsByNameIgnoreCaseAndCategoryId(e.name(), req.categoryId()))
                .map(e -> AssetType.builder()
                        .name(e.name())
                        .description(e.description())
                        .category(category)
                        .build())
                .toList();

        List<AssetTypeResponse> created = typeRepo.saveAll(toSave).stream()
                .map(t -> new AssetTypeResponse(
                        t.getId(), 
                        t.getName(), 
                        t.getDescription(),
                        category.getId(), 
                        category.getName()
                ))
                .toList();

        String message = "Created " + created.size() + " type(s)" +
                (skipped.isEmpty() ? "." : ". " + skipped.size() + " skipped (already exist): " + skipped);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(message, new BulkCreateTypeResult(created, skipped)));
    }
}
