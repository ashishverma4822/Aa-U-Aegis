package com.lostsetbit.aegis_ai.ratelimit.model;

import com.lostsetbit.aegis_ai.ratelimit.entity.RateLimitRule;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RateLimitContext {
    private RateLimitRule rule;
    private String projectApiKey;
    private String clientIdentifier;
}