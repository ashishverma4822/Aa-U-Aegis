package com.lostsetbit.aegis_ai.ratelimit.mapper;

import com.lostsetbit.aegis_ai.ratelimit.dto.CreateRateLimitRuleRequest;
import com.lostsetbit.aegis_ai.ratelimit.dto.RateLimitRuleResponse;
import com.lostsetbit.aegis_ai.ratelimit.entity.RateLimitRule;
import org.springframework.stereotype.Component;

@Component
public class RateLimitRuleMapper {

    public RateLimitRule toEntity(CreateRateLimitRuleRequest req){
        return RateLimitRule.builder()
                .endpoint(req.getEndpoint())
                .limitType(req.getLimitType())
                .requestLimit(req.getRequestLimit())
                .timeWindow(req.getTimeWindow())
                .algorithm(req.getAlgorithm())
                .build();
    }

    public RateLimitRuleResponse toResponse(RateLimitRule rule) {
        return RateLimitRuleResponse.builder()
                .id(rule.getId())
                .projectId(rule.getProject().getId())
                .endpoint(rule.getEndpoint())
                .limitType(rule.getLimitType())
                .requestLimit(rule.getRequestLimit())
                .timeWindow(rule.getTimeWindow())
                .algorithm(rule.getAlgorithm())
                .createdAt(rule.getCreatedAt())
                .updatedAt(rule.getUpdatedAt())
                .build();
    }
}
