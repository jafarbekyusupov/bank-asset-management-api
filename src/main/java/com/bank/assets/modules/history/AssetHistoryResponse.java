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
        Instant changedAt,
        String reason,
        Map<String, Object> metadata
) {
    public record AssetInfo(UUID id, String name, String serialNumber) {}
    public record UserInfo(UUID id, String fullName, String email) {}

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
                h.getChangedAt(),
                h.getReason(),
                h.getMetadata()
        );
    }
}
