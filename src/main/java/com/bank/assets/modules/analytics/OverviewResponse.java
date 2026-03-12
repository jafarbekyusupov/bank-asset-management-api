package com.bank.assets.modules.analytics;

import java.util.List;
import java.util.Map;

public record OverviewResponse(
        AssetStats assets,
        List<CategoryStat> byCategory,
        List<DeptStat> byDepartment,
        WarrantyAlerts warrantyAlerts,
        UserStats users
) {
    public record AssetStats(
        long total,
        Map<String, Long> byStatus // e.g REGISTERED": 12, ASSIGNED: 30
    ) {}

    public record CategoryStat(
        String category,
        long count
    ) {}

    public record DeptStat(
        String department,
        long count
    ) {}

    public record WarrantyAlerts(
        long expired,    // warranty_until < today
        long expiringSoon // warranty_until within 30 days
    ) {}

    public record UserStats(
        long total,
        long active,
        long pendingVerification,
        long pendingApproval
    ) {}
}
