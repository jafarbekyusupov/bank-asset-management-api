package com.bank.assets.modules.asset.dto;

import com.bank.assets.common.enums.AssetAction;
import com.bank.assets.common.enums.AssetStatus;
import com.bank.assets.modules.assignment.AssetAssignment;
import com.bank.assets.modules.history.AssetHistory;

import java.time.Instant;
import java.util.UUID;

public record AssetNoteResponse(
        UUID id,
        String type,
        String content,
        AuthorInfo author,
        Instant date,
        AssetAction action,
        AssetStatus oldStatus,
        AssetStatus newStatus
) {
    public record AuthorInfo(UUID id, String fullName, String email) {}

    public static AssetNoteResponse fromHistory(AssetHistory h) {
        return new AssetNoteResponse(
                h.getId(),
                "STATUS_CHANGE",
                h.getReason(),
                new AuthorInfo(h.getChangedBy().getId(), h.getChangedBy().getFullName(), h.getChangedBy().getEmail()),
                h.getChangedAt(),
                h.getAction(),
                h.getOldStatus(),
                h.getNewStatus()
        );
    }

    public static AssetNoteResponse fromAssignment(AssetAssignment a) {
        AuthorInfo author = a.getAssignedToUser() != null
                ? new AuthorInfo(a.getAssignedToUser().getId(), a.getAssignedToUser().getFullName(), a.getAssignedToUser().getEmail())
                : new AuthorInfo(a.getAssignedBy().getId(), a.getAssignedBy().getFullName(), a.getAssignedBy().getEmail());
        return new AssetNoteResponse(
                a.getId(),
                "RETURN_NOTE",
                a.getReturnNotes(),
                author,
                a.getReturnedAt(),
                null,
                null,
                null
        );
    }
}
