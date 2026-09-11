package com.lostsetbit.aegis_ai.ratelimit.resolver;

import com.lostsetbit.aegis_ai.project.entity.Project;
import com.lostsetbit.aegis_ai.project.repository.ProjectRepository;
import com.lostsetbit.aegis_ai.ratelimit.entity.RateLimitRule;
import com.lostsetbit.aegis_ai.ratelimit.repository.RateLimitRuleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class RateLimitRuleResolver {

    private final ProjectRepository projectRepository;
    private final RateLimitRuleRepository ruleRepository;

    public Optional<Project> resolveActiveProject(String apiKey) {
        if (apiKey == null || apiKey.isBlank()) {
            return Optional.empty();
        }
        return projectRepository.findByApiKeyAndIsActiveTrue(apiKey);
    }

    public Optional<RateLimitRule> resolveRule(Project project, String endpoint) {
        if (project == null || project.getId() == null || endpoint == null || endpoint.isBlank()) {
            return Optional.empty();
        }
        return ruleRepository.findByProjectIdAndEndpoint(project.getId(), endpoint);
    }
}