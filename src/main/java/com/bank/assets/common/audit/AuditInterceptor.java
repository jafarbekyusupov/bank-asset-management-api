package com.bank.assets.common.audit;

import com.bank.assets.modules.audit.AuditLog;
import com.bank.assets.modules.audit.AuditLogRepository;
import com.bank.assets.modules.user.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuditInterceptor implements HandlerInterceptor {
    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;

    private static final Set<String> MUTATING_METHODS = Set.of("POST", "PUT", "PATCH", "DELETE");
    private static final Pattern UUID_PATTERN = Pattern.compile(
            "[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}",
            Pattern.CASE_INSENSITIVE
    );

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        request.setAttribute("_audit_start", System.currentTimeMillis());
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        if (!MUTATING_METHODS.contains(request.getMethod())) return;

        try {
            Long start = (Long) request.getAttribute("_audit_start");
            int duration = start != null ? (int) (System.currentTimeMillis() - start) : 0;

            UUID userId = null;
            String userEmail = null;
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getPrincipal() instanceof User user) {
                userId = user.getId();
                userEmail = user.getEmail();
            }

            String uri = request.getRequestURI();
            Map<String, Object> payload = extractPayload(request);

            AuditLog entry = AuditLog.builder()
                .userId(userId)
                .userEmail(userEmail)
                .httpMethod(request.getMethod())
                .endpoint(uri)
                .entityType(resolveEntityType(uri))
                .entityId(resolveEntityId(uri))
                .ipAddress(resolveClientIp(request))
                .userAgent(request.getHeader("User-Agent"))
                .responseStatus(response.getStatus())
                .durationMs(duration)
                .requestPayload(payload)
                .build();
            auditLogRepository.save(entry);
        } catch (Exception e) {
            log.error("Audit logging failed for {} {}: {}", request.getMethod(), request.getRequestURI(), e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> extractPayload(HttpServletRequest request) {
        if (!(request instanceof ContentCachingRequestWrapper wrapper)) return null;
        byte[] body = wrapper.getContentAsByteArray();
        if (body.length == 0) return null;
        try {
            Map<String, Object> map = objectMapper.readValue(body, Map.class);
            map.remove("password");
            map.remove("passwordHash");
            map.remove("code");
            return map;
        } catch (Exception e) {
            return null;
        }
    }

    private String resolveEntityType(String uri) {
        if (uri.contains("/assets"))      return "ASSET";
        if (uri.contains("/users"))       return "USER";
        if (uri.contains("/branches"))    return "BRANCH";
        if (uri.contains("/departments")) return "DEPARTMENT";
        if (uri.contains("/auth"))        return "AUTH";
        return null;
    }

    private String resolveEntityId(String uri) {
        Matcher m = UUID_PATTERN.matcher(uri);
        return m.find() ? m.group() : null;
    }

    private String resolveClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
