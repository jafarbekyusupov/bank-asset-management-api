package com.bank.assets.modules.analytics;

import com.bank.assets.common.response.ApiResponse;
import com.bank.assets.modules.asset.dto.AssetResponse;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Tag(name="Analytics")
@RequestMapping("/analytics")
@RequiredArgsConstructor
public class AnalyticsController {
    private final AnalyticsService analyticsService;

    @GetMapping("/overview")
    public ResponseEntity<ApiResponse<OverviewResponse>> getOverview() {
        return ResponseEntity.ok(ApiResponse.ok(analyticsService.getOverview()));
    }

    @GetMapping("/assets/expiring")
    public ResponseEntity<ApiResponse<List<AssetResponse>>> getExpiringAssets(
        @RequestParam(defaultValue="30") int days
    ) {
        return ResponseEntity.ok(ApiResponse.ok(analyticsService.getExpiringAssets(days)));
    }
}
