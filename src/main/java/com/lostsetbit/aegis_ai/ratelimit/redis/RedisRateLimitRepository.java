package com.lostsetbit.aegis_ai.ratelimit.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Repository;

import java.util.Collections;

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

    public long incrementAndGet(String key, long windowSeconds) {
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
}