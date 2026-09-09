package com.lostsetbit.aegis_ai.ratelimit.service;

import com.lostsetbit.aegis_ai.ratelimit.entity.RateLimitRule;
import com.lostsetbit.aegis_ai.ratelimit.model.RateLimitResult;
import com.lostsetbit.aegis_ai.ratelimit.redis.RedisRateLimitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@RequiredArgsConstructor
public class FixedWindowRateLimiter {

    private final RedisRateLimitRepository redisRepository;

    public RateLimitResult evaluate(RateLimitRule rule, String projectApiKey, String clientIdentifier) {
        String resolvedClientId = resolveClientIdentifier(rule, projectApiKey, clientIdentifier);

        long timeWindow = rule.getTimeWindow();
        long currentEpochSeconds = Instant.now().getEpochSecond();
        long window = currentEpochSeconds / timeWindow;

        String redisKey = String.format("rate_limit:%s:%s:%s:%s:%d",
                projectApiKey,
                rule.getEndpoint(),
                rule.getLimitType().name(),
                resolvedClientId,
                window
        );

        long currentCount = redisRepository.incrementAndGet(redisKey, timeWindow);
        long limit = rule.getRequestLimit();

        if (currentCount > limit) {
            return RateLimitResult.builder()
                    .allowed(false)
                    .limit(limit)
                    .remaining(0)
                    .message("Rate limit exceeded")
                    .build();
        }

        return RateLimitResult.builder()
                .allowed(true)
                .limit(limit)
                .remaining(limit - currentCount)
                .message("Request allowed")
                .build();
    }

    private String resolveClientIdentifier(RateLimitRule rule, String projectApiKey, String providedIdentifier) {
        return switch (rule.getLimitType()) {
            case IP -> (providedIdentifier != null && !providedIdentifier.isBlank()) ? providedIdentifier : "127.0.0.1";
            case ENDPOINT -> "GLOBAL";
            case API_KEY -> projectApiKey;
            case USER -> (providedIdentifier != null && !providedIdentifier.isBlank()) ? providedIdentifier : "ANONYMOUS";
        };
    }
}