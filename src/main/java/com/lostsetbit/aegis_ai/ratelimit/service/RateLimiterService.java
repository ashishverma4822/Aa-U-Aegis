package com.lostsetbit.aegis_ai.ratelimit.service;

import com.lostsetbit.aegis_ai.project.entity.Project;
import com.lostsetbit.aegis_ai.project.repository.ProjectRepository;
import com.lostsetbit.aegis_ai.ratelimit.entity.RateLimitAlgorithm;
import com.lostsetbit.aegis_ai.ratelimit.entity.RateLimitRule;
import com.lostsetbit.aegis_ai.ratelimit.model.RateLimitResult;
import com.lostsetbit.aegis_ai.ratelimit.repository.RateLimitRuleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RateLimiterService {

    private final ProjectRepository projectRepository;
    private final RateLimitRuleRepository ruleRepository;
    private final FixedWindowRateLimiter fixedWindowRateLimiter;

    @Transactional(readOnly = true)
    public RateLimitResult checkRateLimit(UUID projectId, String endpoint, String clientIdentifier, Long userId) {

        Project project = projectRepository.findByIdAndUserId(projectId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));

        RateLimitRule rule = ruleRepository.findByProjectIdAndEndpoint(projectId, endpoint)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No rate limit rule configured for endpoint: " + endpoint));

        if (rule.getAlgorithm() == RateLimitAlgorithm.TOKEN_BUCKET) {
            throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED, "TOKEN_BUCKET algorithm is not supported in this story");
        }

        return fixedWindowRateLimiter.evaluate(rule, project.getApiKey(), clientIdentifier);
    }
}