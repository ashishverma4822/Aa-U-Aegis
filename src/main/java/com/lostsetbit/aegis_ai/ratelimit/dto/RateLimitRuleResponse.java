package com.lostsetbit.aegis_ai.ratelimit.dto;

import com.lostsetbit.aegis_ai.ratelimit.entity.LimitType;
import com.lostsetbit.aegis_ai.ratelimit.entity.RateLimitAlgorithm;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RateLimitRuleResponse {
    private Long id;
    private UUID projectId;
    private String endpoint;
    private LimitType limitType;
    private Integer requestLimit;
    private Integer timeWindow;
    private RateLimitAlgorithm algorithm;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}