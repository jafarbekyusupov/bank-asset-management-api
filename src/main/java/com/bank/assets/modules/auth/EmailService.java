package com.bank.assets.modules.auth;

import com.bank.assets.common.enums.OtpPurpose;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class EmailService {
    private final RestClient restClient;
    private final String from;

    public EmailService(
        @Value("${app.resend.api-key}") String apiKey,
        @Value("${app.email.from}") String from
    ) {
        this.from = from;
        this.restClient = RestClient.builder()
            .baseUrl("https://api.resend.com")
            .defaultHeader("Authorization", "Bearer " + apiKey)
            .defaultHeader("Content-Type", "application/json")
            .build();
    }

    @Async
    public void sendOtp(String to, String fullName, String code, OtpPurpose purpose) {
        try {
            restClient.post()
                .uri("/emails")
                .body(Map.of(
                    "from", from,
                    "to", List.of(to),
                    "subject", subject(purpose),
                    "text", body(fullName, code, purpose)
                ))
                .retrieve()
                .toBodilessEntity();
            log.debug("OTP email sent to {}", to);
        } catch (Exception e) {
            log.error("failed to send OTP email to {}: {}", to, e.getMessage());
        }
    }

    private String subject(OtpPurpose purpose) {
        return switch (purpose) {
            case REGISTRATION -> "Your registration verification code";
            case PASSWORD_RESET -> "Your password reset code";
        };
    }

    private String body(String fullName, String code, OtpPurpose purpose) {
        String action = switch (purpose) {
            case REGISTRATION -> "complete your registration";
            case PASSWORD_RESET -> "reset your password";
        };
        return """
                Hello %s,

                Use the following code to %s:

                    %s

                This code expires in 15 minutes. Do not share it with anyone.

                If you did not request this, please ignore this email.
                """.formatted(fullName, action, code);
    }
}
