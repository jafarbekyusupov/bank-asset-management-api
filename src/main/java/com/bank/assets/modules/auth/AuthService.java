package com.bank.assets.modules.auth;

import com.bank.assets.common.enums.OtpPurpose;
import com.bank.assets.common.enums.UserStatus;
import com.bank.assets.common.exception.AppException;
import com.bank.assets.common.exception.ErrorCode;
import com.bank.assets.common.security.JwtService;
import com.bank.assets.modules.auth.dto.*;
import com.bank.assets.modules.user.User;
import com.bank.assets.modules.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final OtpConfirmationRepository otpRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final EmailService emailService;

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int OTP_EXPIRY_MINUTES = 15;

    @Transactional
    public void register(RegisterRequest req) {
        User user = userRepository
            .findByEmail(req.email())
            .orElseThrow(() -> AppException.notFound(ErrorCode.USER_NOT_FOUND));

        if (user.getStatus() != UserStatus.PENDING) {
            throw AppException.conflict(ErrorCode.USER_ALREADY_EXISTS);
        }

        user.setPasswordHash(passwordEncoder.encode(sha1(req.password())));

        if (user.isDev()) {
            user.setStatus(UserStatus.ACTIVE);
            userRepository.save(user);
        } else {
            userRepository.save(user);
            sendOtp(user, OtpPurpose.REGISTRATION);
        }
    }

    @Transactional
    public AuthResponse verifyOtp(VerifyOtpRequest req) {
        User user = userRepository
            .findByEmail(req.email())
            .orElseThrow(() -> AppException.notFound(ErrorCode.USER_NOT_FOUND));

        OtpConfirmation otp = otpRepository
            .findTopByUserIdAndPurposeAndUsedAtIsNullOrderByCreatedAtDesc(user.getId(), OtpPurpose.REGISTRATION)
            .orElseThrow(() -> AppException.badRequest(ErrorCode.OTP_NOT_FOUND));

        if (otp.getExpiresAt().isBefore(Instant.now())) {
            throw AppException.badRequest(ErrorCode.OTP_EXPIRED);
        }

        if (!otp.getCode().equals(req.code())) {
            throw AppException.badRequest(ErrorCode.OTP_INVALID);
        }

        otp.setUsedAt(Instant.now());
        otpRepository.save(otp);

        user.setStatus(UserStatus.ACTIVE);
        userRepository.save(user);

        return buildAuthResponse(user);
    }

    @Transactional
    public void resendOtp(ResendOtpRequest req) {
        User user = userRepository
            .findByEmail(req.email())
            .orElseThrow(() -> AppException.notFound(ErrorCode.USER_NOT_FOUND));

        if (user.getStatus() != UserStatus.PENDING) {
            return;
        }
        sendOtp(user, OtpPurpose.REGISTRATION);
    }

    public AuthResponse login(LoginRequest req) {
        User user = userRepository
            .findByEmail(req.email())
            .orElseThrow(() -> AppException.unauthorized(ErrorCode.INVALID_CREDENTIALS));

        if (user.getPasswordHash() == null
            || !passwordEncoder.matches(sha1(req.password()), user.getPasswordHash())
        ) {
            throw AppException.unauthorized(ErrorCode.INVALID_CREDENTIALS);
        }

        if (user.getStatus() == UserStatus.PENDING) {
            throw AppException.badRequest(ErrorCode.ACCOUNT_PENDING);
        }

        if (user.getStatus() == UserStatus.SUSPENDED) {
            throw AppException.badRequest(ErrorCode.ACCOUNT_SUSPENDED);
        }

        return buildAuthResponse(user);
    }

    private String sha1(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] hash = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-1 unavailable", e);
        }
    }

    private void sendOtp(User user, OtpPurpose purpose) {
        String code = String.format("%06d", RANDOM.nextInt(1_000_000));
        OtpConfirmation otp = OtpConfirmation.builder()
            .user(user)
            .code(code)
            .purpose(purpose)
            .expiresAt(Instant.now().plus(OTP_EXPIRY_MINUTES, ChronoUnit.MINUTES))
            .build();

        otpRepository.save(otp);
        emailService.sendOtp(user.getEmail(), user.getFullName(), code, purpose);
    }

    private AuthResponse buildAuthResponse(User user) {
        String token = jwtService.generateToken(user);
        return new AuthResponse(
            token,
            "Bearer",
            jwtService.getExpirationMs() / 1000,
            new AuthResponse.UserInfo(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getRole(),
                user.getStatus()
            )
        );
    }
}
