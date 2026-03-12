package com.bank.assets.modules.auth;

import com.bank.assets.common.enums.OtpPurpose;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface OtpConfirmationRepository extends JpaRepository<OtpConfirmation, UUID> {
    Optional<OtpConfirmation> findTopByUserIdAndPurposeAndUsedAtIsNullOrderByCreatedAtDesc(
            UUID userId, OtpPurpose purpose);
}
