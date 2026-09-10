package com.lostsetbit.aegis_ai.ratelimit.service;

import com.lostsetbit.aegis_ai.project.entity.Project;
import com.lostsetbit.aegis_ai.project.repository.ProjectRepository;
import com.lostsetbit.aegis_ai.ratelimit.entity.RateLimitAlgorithm;
import com.lostsetbit.aegis_ai.ratelimit.entity.RateLimitRule;
import com.lostsetbit.aegis_ai.ratelimit.model.RateLimitContext;
import com.lostsetbit.aegis_ai.ratelimit.model.RateLimitResult;
import com.lostsetbit.aegis_ai.ratelimit.repository.RateLimitRuleRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class RateLimiterService {

    private final ProjectRepository projectRepository;
    private final RateLimitRuleRepository ruleRepository;
    private final Map<RateLimitAlgorithm, RateLimiter> rateLimiterStrategies;

    public RateLimiterService(
            ProjectRepository projectRepository,
            RateLimitRuleRepository ruleRepository,
            List<RateLimiter> rateLimiters) {
        this.projectRepository = projectRepository;
        this.ruleRepository = ruleRepository;
        this.rateLimiterStrategies = rateLimiters.stream()
                .collect(Collectors.toMap(RateLimiter::getSupportedAlgorithm, Function.identity()));
    }

    @Transactional(readOnly = true)
    public RateLimitResult checkRateLimit(UUID projectId, String endpoint, String clientIdentifier, Long userId) {
        Project project = projectRepository.findByIdAndUserId(projectId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));

        RateLimitRule rule = ruleRepository.findByProjectIdAndEndpoint(projectId, endpoint)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No rate limit rule configured for endpoint: " + endpoint));

        RateLimiter limiter = rateLimiterStrategies.get(rule.getAlgorithm());
        if (limiter == null) {
            throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED, "Algorithm " + rule.getAlgorithm() + " is not supported");
        }

        RateLimitContext context = RateLimitContext.builder()
                .rule(rule)
                .projectApiKey(project.getApiKey())
                .clientIdentifier(clientIdentifier)
                .build();

        return limiter.evaluate(context);
    }
}