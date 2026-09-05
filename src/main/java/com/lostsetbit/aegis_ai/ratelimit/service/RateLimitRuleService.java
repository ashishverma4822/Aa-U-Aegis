package com.lostsetbit.aegis_ai.ratelimit.service;

import com.lostsetbit.aegis_ai.project.entity.Project;
import com.lostsetbit.aegis_ai.project.repository.ProjectRepository;
import com.lostsetbit.aegis_ai.ratelimit.dto.CreateRateLimitRuleRequest;
import com.lostsetbit.aegis_ai.ratelimit.dto.RateLimitRuleResponse;
import com.lostsetbit.aegis_ai.ratelimit.dto.UpdateRateLimitRuleRequest;
import com.lostsetbit.aegis_ai.ratelimit.entity.RateLimitRule;
import com.lostsetbit.aegis_ai.ratelimit.mapper.RateLimitRuleMapper;
import com.lostsetbit.aegis_ai.ratelimit.repository.RateLimitRuleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RateLimitRuleService {

    private final RateLimitRuleRepository ruleRepository;
    private final ProjectRepository projectRepository;
    private final RateLimitRuleMapper ruleMapper;

    @Transactional
    public RateLimitRuleResponse createRule(UUID projectId, CreateRateLimitRuleRequest request, Long userId) {
        Project project = getProjectWithOwnershipCheck(projectId, userId);

        if (ruleRepository.existsByProjectIdAndEndpointAndLimitType(projectId, request.getEndpoint(), request.getLimitType())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "A rule with the same endpoint and limit type already exists for this project"
            );
        }

        RateLimitRule rule = ruleMapper.toEntity(request);
        rule.setProject(project);

        RateLimitRule savedRule = ruleRepository.save(rule);
        return ruleMapper.toResponse(savedRule);
    }

    @Transactional(readOnly = true)
    public List<RateLimitRuleResponse> getRulesByProjectId(UUID projectId, Long userId) {
        getProjectWithOwnershipCheck(projectId, userId);

        return ruleRepository.findByProjectId(projectId).stream()
                .map(ruleMapper::toResponse)
                .toList();
    }

    @Transactional
    public RateLimitRuleResponse updateRule(Long ruleId, UpdateRateLimitRuleRequest request, Long userId) {
        RateLimitRule rule = ruleRepository.findByIdAndProjectUserId(ruleId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Rule not found"));

        boolean keyChanged = !rule.getEndpoint().equals(request.getEndpoint()) || !rule.getLimitType().equals(request.getLimitType());
        if (keyChanged && ruleRepository.existsByProjectIdAndEndpointAndLimitType(rule.getProject().getId(), request.getEndpoint(), request.getLimitType())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "A rule with the same endpoint and limit type already exists for this project"
            );
        }

        rule.setEndpoint(request.getEndpoint());
        rule.setLimitType(request.getLimitType());
        rule.setRequestLimit(request.getRequestLimit());
        rule.setTimeWindow(request.getTimeWindow());
        rule.setAlgorithm(request.getAlgorithm());

        RateLimitRule updatedRule = ruleRepository.save(rule);
        return ruleMapper.toResponse(updatedRule);
    }

    @Transactional
    public void deleteRule(Long ruleId, Long userId) {
        RateLimitRule rule = ruleRepository.findByIdAndProjectUserId(ruleId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Rule not found"));

        ruleRepository.delete(rule);
    }

    private Project getProjectWithOwnershipCheck(UUID projectId, Long userId) {
        return projectRepository.findByIdAndUserId(projectId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));
    }
}