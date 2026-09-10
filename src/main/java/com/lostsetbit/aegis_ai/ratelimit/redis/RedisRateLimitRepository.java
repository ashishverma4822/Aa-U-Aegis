package com.lostsetbit.aegis_ai.ratelimit.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class RedisRateLimitRepository {

    private final StringRedisTemplate redisTemplate;

    private static final String FIXED_WINDOW_LUA =
            "local current = redis.call('INCR', KEYS[1]) " +
                    "if tonumber(current) == 1 then " +
                    "  redis.call('EXPIRE', KEYS[1], ARGV[1]) " +
                    "end " +
                    "return current";

    private static final String TOKEN_BUCKET_LUA =
            "local key = KEYS[1] " +
                    "local capacity = tonumber(ARGV[1]) " +
                    "local refillInterval = tonumber(ARGV[2]) " +
                    "local now = tonumber(ARGV[3]) " +
                    "local ttl = tonumber(ARGV[4]) " +

                    "local data = redis.call('HMGET', key, 'tokens', 'lastRefillTimestamp') " +
                    "local tokens = tonumber(data[1]) " +
                    "local lastRefillTimestamp = tonumber(data[2]) " +

                    "if tokens == nil then " +
                    "    tokens = capacity " +
                    "    lastRefillTimestamp = now " +
                    "else " +
                    "    local timePassed = math.max(0, now - lastRefillTimestamp) " +
                    "    local refillRate = capacity / refillInterval " +
                    "    local tokensToAdd = timePassed * refillRate " +
                    "    tokens = math.min(capacity, tokens + tokensToAdd) " +
                    "    lastRefillTimestamp = now " +
                    "end " +

                    "local allowed = 0 " +
                    "if tokens >= 1 then " +
                    "    tokens = tokens - 1 " +
                    "    allowed = 1 " +
                    "end " +

                    "redis.call('HMSET', key, 'tokens', tokens, 'lastRefillTimestamp', lastRefillTimestamp) " +
                    "redis.call('EXPIRE', key, ttl) " +

                    "return { allowed, math.floor(tokens) }";

    public long incrementAndGetFixedWindow(String key, long windowSeconds) {
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setScriptText(FIXED_WINDOW_LUA);
        script.setResultType(Long.class);

        Long currentCount = redisTemplate.execute(
                script,
                Collections.singletonList(key),
                String.valueOf(windowSeconds)
        );

        return currentCount != null ? currentCount : 1L;
    }

    @SuppressWarnings("unchecked")
    public List<Long> executeTokenBucket(String key, long capacity, long timeWindow, long nowEpochSeconds, long ttlSeconds) {
        DefaultRedisScript<List> script = new DefaultRedisScript<>();
        script.setScriptText(TOKEN_BUCKET_LUA);
        script.setResultType(List.class);

        List<Long> result = (List<Long>) redisTemplate.execute(
                script,
                Collections.singletonList(key),
                String.valueOf(capacity),
                String.valueOf(timeWindow),
                String.valueOf(nowEpochSeconds),
                String.valueOf(ttlSeconds)
        );

        return result != null ? result : Arrays.asList(0L, 0L);
    }
}