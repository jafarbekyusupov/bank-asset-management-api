package com.bank.assets.modules.auth;

import com.bank.assets.common.enums.OtpPurpose;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {
    private final JavaMailSender mailSender;

    @Value("${app.email.from}")
    private String from;

    @Async
    public void sendOtp(String to, String fullName, String code, OtpPurpose purpose) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(from);
            message.setTo(to);
            message.setSubject(subject(purpose));
            message.setText(body(fullName, code, purpose));
            mailSender.send(message);
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
