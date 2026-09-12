package com.lostsetbit.aegis_ai.analytics.event;

import com.lostsetbit.aegis_ai.analytics.entity.RateLimitStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class TrafficMetricEvent {
    private final UUID projectId;
    private final String endpoint;
    private final String method;
    private final Integer statusCode;
    private final Long responseTime;
    private final String clientIdentifier;
    private final RateLimitStatus rateLimitStatus;
    private final LocalDateTime timestamp;
}