package com.lostsetbit.aegis_ai.ratelimit.controller;

import com.lostsetbit.aegis_ai.auth.entity.User;
import com.lostsetbit.aegis_ai.ratelimit.dto.CheckRateLimitRequest;
import com.lostsetbit.aegis_ai.ratelimit.model.RateLimitResult;
import com.lostsetbit.aegis_ai.ratelimit.service.RateLimiterService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/projects/{projectId}/rate-limit")
@RequiredArgsConstructor
public class RateLimitCheckController {

    private final RateLimiterService rateLimiterService;

    @PostMapping("/check")
    public ResponseEntity<RateLimitResult> checkRateLimit(
            @PathVariable UUID projectId,
            @Valid @RequestBody CheckRateLimitRequest request,
            @AuthenticationPrincipal User currentUser) {

        RateLimitResult result = rateLimiterService.checkRateLimit(
                projectId,
                request.getEndpoint(),
                request.getClientIdentifier(),
                currentUser.getId()
        );

        if (!result.isAllowed()) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(result);
        }

        return ResponseEntity.ok(result);
    }
}