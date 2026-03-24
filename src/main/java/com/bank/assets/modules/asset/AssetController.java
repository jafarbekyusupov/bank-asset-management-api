package com.bank.assets.modules.asset;

import com.bank.assets.common.enums.AssetStatus;
import com.bank.assets.common.enums.UserRole;
import com.bank.assets.common.response.ApiResponse;
import com.bank.assets.common.response.PageResponse;
import com.bank.assets.modules.ai.AiSummarizerService;
import com.bank.assets.modules.ai.dto.SummarizeResponse;
import com.bank.assets.modules.asset.dto.AssetNoteResponse;
import com.bank.assets.modules.asset.dto.AssetResponse;
import com.bank.assets.modules.asset.dto.CreateAssetRequest;
import com.bank.assets.modules.asset.dto.UpdateAssetRequest;
import com.bank.assets.modules.user.User;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@Tag(name="Asset")
@RequestMapping("/asset")
@RequiredArgsConstructor
public class AssetController {
    private final AssetService assetService;
    private final StorageService storageService;
    private final AiSummarizerService summarizerService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<AssetResponse>> create(
        @Valid @RequestBody CreateAssetRequest req,
        @AuthenticationPrincipal User currentUser
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
            ApiResponse.ok("Asset created.", assetService.create(req, currentUser))
        );
    }

    @GetMapping("/list")
    public ResponseEntity<ApiResponse<PageResponse<AssetResponse>>> list(
        @RequestParam(required = false) AssetStatus status,
        @RequestParam(required = false) UUID categoryId,
        @RequestParam(required = false) UUID typeId,
        @RequestParam(required = false) UUID ownerId,
        @RequestParam(required = false) UUID deptId,
        @RequestParam(required = false) String search,
        @AuthenticationPrincipal User currentUser,
        @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
            PageResponse.from(assetService.list(status, categoryId, typeId, ownerId, deptId, search, currentUser, pageable))
        ));
    }

    @GetMapping("/my")
    public ResponseEntity<ApiResponse<PageResponse<AssetResponse>>> myAssets(
        @AuthenticationPrincipal User currentUser,
        @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        UUID ownerId = currentUser.getRole() == UserRole.STAFF ? currentUser.getId() : null;
        return ResponseEntity.ok(ApiResponse.ok(
            PageResponse.from(assetService.list(null, null, null, ownerId, null, null, currentUser, pageable))
        ));
    }

    @GetMapping("/assignable")
    public ResponseEntity<ApiResponse<PageResponse<AssetResponse>>> assignable(
        @AuthenticationPrincipal User currentUser,
        @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
            PageResponse.from(assetService.listAssignable(currentUser, pageable))
        ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AssetResponse>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(assetService.getById(id)));
    }

    @GetMapping("/{id}/notes")
    public ResponseEntity<ApiResponse<List<AssetNoteResponse>>> getNotes(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(assetService.getNotes(id)));
    }

    @PostMapping("/{id}/notes/summarize")
    public ResponseEntity<ApiResponse<SummarizeResponse>> summarizeNotes(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(new SummarizeResponse(summarizerService.summarize(id))));
    }

    @GetMapping("/serial/{serialNumber}")
    public ResponseEntity<ApiResponse<AssetResponse>> getBySerialNumber(@PathVariable String serialNumber) {
        return ResponseEntity.ok(ApiResponse.ok(assetService.getBySerialNumber(serialNumber)));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<AssetResponse>> update(
        @PathVariable UUID id,
        @RequestBody UpdateAssetRequest req,
        @AuthenticationPrincipal User currentUser
    ) {
        return ResponseEntity.ok(ApiResponse.ok("Asset updated.", assetService.update(id, req, currentUser)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        assetService.delete(id);
        return ResponseEntity.ok(ApiResponse.ok("Asset deleted."));
    }

    @GetMapping(value = "/{id}/image", produces = MediaType.IMAGE_JPEG_VALUE)
    public ResponseEntity<byte[]> getImage(@PathVariable UUID id) {
        Asset asset = assetService.findOrThrow(id);
        if (asset.getImageUrl() == null) {
            return ResponseEntity.notFound().build();
        }
        byte[] bytes = storageService.getAssetImageBytes(id);
        if (bytes == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG).body(bytes);
    }

    @PostMapping(value = "/{id}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<AssetResponse>> uploadImage(
        @PathVariable UUID id,
        @RequestPart("file") MultipartFile file,
        @AuthenticationPrincipal User currentUser
    ) {
        storageService.uploadAssetImage(id, file, currentUser);
        return ResponseEntity.ok(ApiResponse.ok("Image uploaded.", assetService.getById(id)));
    }
}
