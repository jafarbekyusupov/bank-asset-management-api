package com.bank.assets.modules.history;

import com.bank.assets.common.enums.AssetAction;
import com.bank.assets.common.enums.AssetStatus;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record AssetHistoryResponse(
        UUID id,
        AssetInfo asset,
        AssetAction action,
        AssetStatus oldStatus,
        AssetStatus newStatus,
        UserInfo fromUser,
        UserInfo toUser,
        UserInfo changedBy,
        DeptInfo fromDept,
        DeptInfo toDept,
        BranchInfo fromBranch,
        BranchInfo toBranch,
        Instant changedAt,
        String reason,
        Map<String, Object> metadata
) {
    public record AssetInfo(UUID id, String name, String serialNumber) {}
    public record UserInfo(UUID id, String fullName, String email) {}
    public record DeptInfo(UUID id, String name) {}
    public record BranchInfo(UUID id, String name) {}

    public static AssetHistoryResponse from(AssetHistory h) {
        return new AssetHistoryResponse(
                h.getId(),
                h.getAsset() != null
                        ? new AssetInfo(h.getAsset().getId(), h.getAsset().getName(), h.getAsset().getSerialNumber())
                        : null,
                h.getAction(),
                h.getOldStatus(),
                h.getNewStatus(),
                h.getFromUser() != null
                        ? new UserInfo(h.getFromUser().getId(), h.getFromUser().getFullName(), h.getFromUser().getEmail())
                        : null,
                h.getToUser() != null
                        ? new UserInfo(h.getToUser().getId(), h.getToUser().getFullName(), h.getToUser().getEmail())
                        : null,
                h.getChangedBy() != null
                        ? new UserInfo(h.getChangedBy().getId(), h.getChangedBy().getFullName(), h.getChangedBy().getEmail())
                        : null,
                h.getFromDept() != null
                        ? new DeptInfo(h.getFromDept().getId(), h.getFromDept().getName())
                        : null,
                h.getToDept() != null
                        ? new DeptInfo(h.getToDept().getId(), h.getToDept().getName())
                        : null,
                h.getFromBranch() != null
                        ? new BranchInfo(h.getFromBranch().getId(), h.getFromBranch().getName())
                        : null,
                h.getToBranch() != null
                        ? new BranchInfo(h.getToBranch().getId(), h.getToBranch().getName())
                        : null,
                h.getChangedAt(),
                h.getReason(),
                h.getMetadata()
        );
    }
}
