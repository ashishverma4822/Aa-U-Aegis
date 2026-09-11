package com.lostsetbit.aegis_ai.ratelimit.model;

import com.lostsetbit.aegis_ai.ratelimit.entity.RateLimitRule;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RateLimitContext {
    private RateLimitRule rule;
    private String projectApiKey;
    private String clientIdentifier;

    public static String extractClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            return xForwardedFor.split(",")[0].trim();
        }
        String remoteAddr = request.getRemoteAddr();
        return (remoteAddr != null && !remoteAddr.isBlank()) ? remoteAddr : "127.0.0.1";
    }
}