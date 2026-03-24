package com.bank.assets.modules.assignment;

import com.bank.assets.modules.asset.Asset;
import com.bank.assets.modules.branch.Branch;
import com.bank.assets.modules.branch.Department;
import com.bank.assets.modules.user.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "asset_assignments")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AssetAssignment {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asset_id", nullable = false)
    private Asset asset;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to_user_id")
    private User assignedToUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to_dept_id")
    private Department assignedToDept;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to_branch_id")
    private Branch assignedToBranch;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_by", nullable = false)
    private User assignedBy;

    @CreationTimestamp
    @Column(name = "assigned_at", nullable = false, updatable = false)
    private Instant assignedAt;

    @Column(name = "acknowledged_at")
    private Instant acknowledgedAt;

    @Column(name = "returned_at")
    private Instant returnedAt;

    @Column(name = "return_notes", columnDefinition = "TEXT")
    private String returnNotes;

    @Column(name = "system_note", nullable = false)
    @Builder.Default
    private boolean systemNote = false;

    @Column(columnDefinition = "TEXT")
    private String notes;
}
