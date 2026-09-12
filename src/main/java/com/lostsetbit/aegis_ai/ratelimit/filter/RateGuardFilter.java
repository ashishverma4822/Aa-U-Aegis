package com.lostsetbit.aegis_ai.ratelimit.filter;

import com.lostsetbit.aegis_ai.analytics.entity.RateLimitStatus;
import com.lostsetbit.aegis_ai.analytics.event.TrafficMetricEvent;
import com.lostsetbit.aegis_ai.project.entity.Project;
import com.lostsetbit.aegis_ai.ratelimit.entity.RateLimitAlgorithm;
import com.lostsetbit.aegis_ai.ratelimit.entity.RateLimitRule;
import com.lostsetbit.aegis_ai.ratelimit.model.RateLimitContext;
import com.lostsetbit.aegis_ai.ratelimit.model.RateLimitResult;
import com.lostsetbit.aegis_ai.ratelimit.resolver.RateLimitRuleResolver;
import com.lostsetbit.aegis_ai.ratelimit.service.RateLimiter;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class RateGuardFilter extends OncePerRequestFilter {

    public static final String HEADER_API_KEY = "X-RateGuard-API-Key";
    public static final String HEADER_LIMIT = "X-RateLimit-Limit";
    public static final String HEADER_REMAINING = "X-RateLimit-Remaining";

    private final RateLimitRuleResolver ruleResolver;
    private final List<RateLimiter> rateLimiters;
    private final ApplicationEventPublisher eventPublisher;

    private Map<RateLimitAlgorithm, RateLimiter> strategyMap;

    @PostConstruct
    public void initStrategies() {
        this.strategyMap = rateLimiters.stream()
                .collect(Collectors.toUnmodifiableMap(RateLimiter::getSupportedAlgorithm, Function.identity()));
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/api/auth") ||
                path.startsWith("/api/projects") ||
                path.startsWith("/api/rate-limits") ||
                path.startsWith("/actuator") ||
                path.startsWith("/swagger-ui") ||
                path.startsWith("/v3/api-docs");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        long startTime = System.currentTimeMillis();

        String apiKey = request.getHeader(HEADER_API_KEY);
        if (apiKey == null || apiKey.isBlank()) {
            apiKey = request.getHeader("X-API-Key");
        }

        if (apiKey == null || apiKey.isBlank()) {
            writeErrorResponse(response, HttpStatus.UNAUTHORIZED, "Missing RateGuard API key header: " + HEADER_API_KEY);
            return;
        }

        Optional<Project> projectOpt;
        try {
            projectOpt = ruleResolver.resolveActiveProject(apiKey);
        } catch (Exception e) {
            writeErrorResponse(response, HttpStatus.INTERNAL_SERVER_ERROR, "Error resolving project details");
            return;
        }

        if (projectOpt.isEmpty()) {
            writeErrorResponse(response, HttpStatus.UNAUTHORIZED, "Invalid or inactive RateGuard API key");
            return;
        }

        Project project = projectOpt.get();
        String requestPath = request.getRequestURI();

        Optional<RateLimitRule> ruleOpt = ruleResolver.resolveRule(project, requestPath);

        if (ruleOpt.isEmpty()) {
            filterChain.doFilter(request, response);
            return;
        }

        RateLimitRule rule = ruleOpt.get();

        String clientIdentifier = resolveClientIdentifier(rule, project.getApiKey(), request);

        RateLimiter limiter = strategyMap.get(rule.getAlgorithm());
        if (limiter == null) {
            writeErrorResponse(response, HttpStatus.NOT_IMPLEMENTED, "Unsupported algorithm: " + rule.getAlgorithm());
            return;
        }

        RateLimitContext context = RateLimitContext.builder()
                .rule(rule)
                .projectApiKey(project.getApiKey())
                .clientIdentifier(clientIdentifier)
                .build();

        RateLimitResult result = limiter.evaluate(context);

        response.setHeader(HEADER_LIMIT, String.valueOf(result.getLimit()));
        response.setHeader(HEADER_REMAINING, String.valueOf(result.getRemaining()));

        if (!result.isAllowed()) {
            writeErrorResponse(response, HttpStatus.TOO_MANY_REQUESTS, result.getMessage());

            publishMetricEvent(
                    project,
                    requestPath,
                    request.getMethod(),
                    HttpStatus.TOO_MANY_REQUESTS.value(),
                    startTime,
                    clientIdentifier,
                    RateLimitStatus.RATE_LIMITED
            );
            return;
        }

        try {
            filterChain.doFilter(request, response);
        } finally {
            publishMetricEvent(
                    project,
                    requestPath,
                    request.getMethod(),
                    response.getStatus(),
                    startTime,
                    clientIdentifier,
                    RateLimitStatus.ALLOWED
            );
        }
    }

    private void publishMetricEvent(
            Project project,
            String endpoint,
            String method,
            int statusCode,
            long startTime,
            String clientIdentifier,
            RateLimitStatus status) {

        long responseTime = System.currentTimeMillis() - startTime;

        TrafficMetricEvent event = TrafficMetricEvent.builder()
                .projectId(project.getId())
                .endpoint(endpoint)
                .method(method)
                .statusCode(statusCode)
                .responseTime(responseTime)
                .clientIdentifier(clientIdentifier)
                .rateLimitStatus(status)
                .timestamp(LocalDateTime.now())
                .build();

        eventPublisher.publishEvent(event);
    }

    private String resolveClientIdentifier(RateLimitRule rule, String apiKey, HttpServletRequest request) {
        return switch (rule.getLimitType()) {
            case IP -> RateLimitContext.extractClientIp(request);
            case ENDPOINT -> rule.getEndpoint();
            case API_KEY -> apiKey;
            case USER -> {
                String userHeader = request.getHeader("X-User-Id");
                yield (userHeader != null && !userHeader.isBlank()) ? userHeader : RateLimitContext.extractClientIp(request);
            }
        };
    }

    private void writeErrorResponse(HttpServletResponse response, HttpStatus status, String message) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        String jsonPayload = String.format("{\"status\":%d,\"error\":\"%s\",\"message\":\"%s\"}",
                status.value(),
                status.getReasonPhrase(),
                message.replace("\"", "\\\""));

        response.getWriter().write(jsonPayload);
    }
}