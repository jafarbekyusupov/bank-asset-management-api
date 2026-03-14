package com.bank.assets.modules.analytics;

import com.bank.assets.common.enums.AssetStatus;
import com.bank.assets.common.enums.UserStatus;
import com.bank.assets.modules.asset.Asset;
import com.bank.assets.modules.asset.AssetRepository;
import com.bank.assets.modules.asset.AssetService;
import com.bank.assets.modules.asset.AssetTypeRepository;
import com.bank.assets.modules.asset.dto.AssetResponse;
import com.bank.assets.modules.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnalyticsService {
    private final AssetRepository assetRepository;
    private final AssetTypeRepository typeRepository;
    private final AssetService assetService;
    private final UserRepository userRepository;

    private static final List<AssetStatus> INACTIVE_STATUSES = List.of(
            AssetStatus.LOST, AssetStatus.WRITTEN_OFF
    );

    @Transactional(readOnly = true)
    public OverviewResponse getOverview() {
        LocalDate today = LocalDate.now();
        LocalDate in30Days = today.plusDays(30);

        Map<String, Long> byStatus = assetRepository.countGroupedByStatus().stream()
                .collect(Collectors.toMap(
                        s -> s.getStatus().name(),
                        AssetRepository.StatusCount::getCount
                ));

        long totalAssets = byStatus.values().stream().mapToLong(Long::longValue).sum();

        Map<String, Long> typesPerCategory = typeRepository.countTypesByCategory().stream()
                .collect(Collectors.toMap(
                        AssetTypeRepository.TypeCountByCategory::getCategoryName,
                        AssetTypeRepository.TypeCountByCategory::getCount
                ));

        List<OverviewResponse.CategoryStat> byCategory = assetRepository.countGroupedByCategoryAndStatus()
                .stream()
                .collect(Collectors.groupingBy(AssetRepository.CategoryStatusCount::getCategoryName))
                .entrySet().stream()
                .map(e -> {
                    String name = e.getKey();
                    String desc = e.getValue().get(0).getDescription();
                    long total = e.getValue().stream().mapToLong(AssetRepository.CategoryStatusCount::getCount).sum();
                    long typeCount = typesPerCategory.getOrDefault(name, 0L);
                    Map<String, Long> statusMap = e.getValue().stream().collect(Collectors.toMap(
                            c -> c.getStatus().name(),
                            AssetRepository.CategoryStatusCount::getCount
                    ));
                    return new OverviewResponse.CategoryStat(name, desc, total, typeCount, statusMap);
                })
                .toList();

        List<OverviewResponse.DeptStat> byDept = assetRepository.countGroupedByDepartment().stream()
                .map(d -> new OverviewResponse.DeptStat(d.getDeptName(), d.getCount()))
                .toList();

        long expired = assetRepository.countWithExpiredWarranty(today, INACTIVE_STATUSES);
        long expiringSoon = assetRepository.countWithWarrantyExpiringSoon(today, in30Days, INACTIVE_STATUSES);

        long totalUsers = userRepository.count();
        long activeUsers = userRepository.countByStatus(UserStatus.ACTIVE);
        long pendingVerification = userRepository.countByStatus(UserStatus.PENDING);
        long pendingApproval = userRepository.countByStatus(UserStatus.PENDING_APPROVAL);

        return new OverviewResponse(
                new OverviewResponse.AssetStats(totalAssets, byStatus),
                byCategory,
                byDept,
                new OverviewResponse.WarrantyAlerts(expired, expiringSoon),
                new OverviewResponse.UserStats(totalUsers, activeUsers, pendingVerification, pendingApproval)
        );
    }

    @Transactional(readOnly = true)
    public List<AssetResponse> getExpiringAssets(int daysAhead) {
        LocalDate threshold = LocalDate.now().plusDays(daysAhead);
        List<Asset> assets = assetRepository.findWithWarrantyUpTo(threshold, INACTIVE_STATUSES);
        return assets.stream().map(assetService::buildResponse).toList();
    }
}
