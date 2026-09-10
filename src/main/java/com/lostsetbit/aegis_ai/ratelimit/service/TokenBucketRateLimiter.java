package com.lostsetbit.aegis_ai.ratelimit.service;

import com.lostsetbit.aegis_ai.ratelimit.entity.RateLimitAlgorithm;
import com.lostsetbit.aegis_ai.ratelimit.entity.RateLimitRule;
import com.lostsetbit.aegis_ai.ratelimit.model.RateLimitContext;
import com.lostsetbit.aegis_ai.ratelimit.model.RateLimitResult;
import com.lostsetbit.aegis_ai.ratelimit.redis.RedisRateLimitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Component
@RequiredArgsConstructor
public class TokenBucketRateLimiter implements RateLimiter {

    private final RedisRateLimitRepository redisRepository;

    @Override
    public RateLimitAlgorithm getSupportedAlgorithm() {
        return RateLimitAlgorithm.TOKEN_BUCKET;
    }

    @Override
    public RateLimitResult evaluate(RateLimitContext context) {
        RateLimitRule rule = context.getRule();
        String resolvedClientId = resolveClientIdentifier(rule, context.getProjectApiKey(), context.getClientIdentifier());

        long capacity = rule.getRequestLimit();
        long timeWindow = rule.getTimeWindow();
        long nowEpochSeconds = Instant.now().getEpochSecond();
        long ttlSeconds = timeWindow * 2;

        String redisKey = String.format("rate_limit:token_bucket:%s:%s:%s:%s",
                context.getProjectApiKey(),
                rule.getEndpoint(),
                rule.getLimitType().name(),
                resolvedClientId
        );

        List<Long> evalResult = redisRepository.executeTokenBucket(
                redisKey,
                capacity,
                timeWindow,
                nowEpochSeconds,
                ttlSeconds
        );

        boolean allowed = evalResult.get(0) == 1L;
        long remainingTokens = evalResult.get(1);

        if (!allowed) {
            return RateLimitResult.builder()
                    .allowed(false)
                    .limit(capacity)
                    .remaining(0)
                    .message("Rate limit exceeded")
                    .build();
        }

        return RateLimitResult.builder()
                .allowed(true)
                .limit(capacity)
                .remaining(remainingTokens)
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