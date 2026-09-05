package com.lostsetbit.aegis_ai.ratelimit.controller;

import com.lostsetbit.aegis_ai.auth.entity.User;
import com.lostsetbit.aegis_ai.ratelimit.dto.CreateRateLimitRuleRequest;
import com.lostsetbit.aegis_ai.ratelimit.dto.RateLimitRuleResponse;
import com.lostsetbit.aegis_ai.ratelimit.dto.UpdateRateLimitRuleRequest;
import com.lostsetbit.aegis_ai.ratelimit.service.RateLimitRuleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class RateLimitRuleController {

    private final RateLimitRuleService ruleService;

    @PostMapping("/projects/{projectId}/rules")
    public ResponseEntity<RateLimitRuleResponse> createRule(
            @PathVariable UUID projectId,
            @Valid @RequestBody CreateRateLimitRuleRequest request,
            @AuthenticationPrincipal User authenticatedUser) {

        RateLimitRuleResponse createdRule = ruleService.createRule(projectId, request, authenticatedUser.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(createdRule);
    }

    @GetMapping("/projects/{projectId}/rules")
    public ResponseEntity<List<RateLimitRuleResponse>> getProjectRules(
            @PathVariable UUID projectId,
            @AuthenticationPrincipal User authenticatedUser) {

        List<RateLimitRuleResponse> rules = ruleService.getRulesByProjectId(projectId, authenticatedUser.getId());
        return ResponseEntity.ok(rules);
    }

    @PutMapping("/rules/{ruleId}")
    public ResponseEntity<RateLimitRuleResponse> updateRule(
            @PathVariable Long ruleId,
            @Valid @RequestBody UpdateRateLimitRuleRequest request,
            @AuthenticationPrincipal User authenticatedUser) {

        RateLimitRuleResponse updatedRule = ruleService.updateRule(ruleId, request, authenticatedUser.getId());
        return ResponseEntity.ok(updatedRule);
    }

    @DeleteMapping("/rules/{ruleId}")
    public ResponseEntity<Map<String, String>> deleteRule(
            @PathVariable Long ruleId,
            @AuthenticationPrincipal User authenticatedUser) {

        ruleService.deleteRule(ruleId, authenticatedUser.getId());
        return ResponseEntity.ok(Map.of("message", "Rate limit rule deleted successfully"));
    }
}