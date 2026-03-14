package com.bank.assets.modules.history;

import com.bank.assets.common.response.ApiResponse;
import com.bank.assets.common.response.PageResponse;
import com.bank.assets.modules.user.User;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@Tag(name = "Asset History")
@RequiredArgsConstructor
public class AssetHistoryController {
    private final AssetHistoryService assetHistoryService;

    @GetMapping("/history/{id}")
    public ResponseEntity<ApiResponse<PageResponse<AssetHistoryResponse>>> getAssetHistory(
        @PathVariable UUID id,
        @PageableDefault(size = 20, sort = "changedAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
            PageResponse.from(assetHistoryService.getAssetHistory(id, pageable))
        ));
    }

    @GetMapping("/history/my")
    public ResponseEntity<ApiResponse<PageResponse<AssetHistoryResponse>>> myActivity(
        @AuthenticationPrincipal User currentUser,
        @PageableDefault(size = 20, sort = "changedAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
            PageResponse.from(assetHistoryService.getMyActivity(currentUser, pageable))
        ));
    }
}
