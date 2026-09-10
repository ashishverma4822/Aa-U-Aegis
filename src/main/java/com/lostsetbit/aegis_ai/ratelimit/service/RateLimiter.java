package com.lostsetbit.aegis_ai.ratelimit.service;

import com.lostsetbit.aegis_ai.ratelimit.entity.RateLimitAlgorithm;
import com.lostsetbit.aegis_ai.ratelimit.model.RateLimitContext;
import com.lostsetbit.aegis_ai.ratelimit.model.RateLimitResult;

public interface RateLimiter {
    RateLimitAlgorithm getSupportedAlgorithm();
    RateLimitResult evaluate(RateLimitContext context);
}