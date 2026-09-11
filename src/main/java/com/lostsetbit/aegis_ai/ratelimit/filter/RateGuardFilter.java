package com.lostsetbit.aegis_ai.ratelimit.filter;

import com.lostsetbit.aegis_ai.project.entity.Project;
import com.lostsetbit.aegis_ai.ratelimit.entity.RateLimitAlgorithm;
import com.lostsetbit.aegis_ai.ratelimit.entity.RateLimitRule;
import com.lostsetbit.aegis_ai.ratelimit.model.RateLimitContext;
import com.lostsetbit.aegis_ai.ratelimit.model.RateLimitResult;
import com.lostsetbit.aegis_ai.ratelimit.resolver.RateLimitRuleResolver;
import com.lostsetbit.aegis_ai.ratelimit.service.RateLimiter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
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

    private Map<RateLimitAlgorithm, RateLimiter> strategyMap;

    private Map<RateLimitAlgorithm, RateLimiter> getStrategies() {
        if (strategyMap == null) {
            strategyMap = rateLimiters.stream()
                    .collect(Collectors.toMap(RateLimiter::getSupportedAlgorithm, Function.identity()));
        }
        return strategyMap;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/api/auth") ||
                path.startsWith("/api/projects") ||
                path.startsWith("/actuator") ||
                path.startsWith("/swagger-ui") ||
                path.startsWith("/v3/api-docs");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String apiKey = request.getHeader(HEADER_API_KEY);

        if (apiKey == null || apiKey.isBlank()) {
            writeErrorResponse(response, HttpStatus.UNAUTHORIZED, "Missing RateGuard API key header: " + HEADER_API_KEY);
            return;
        }

        Optional<Project> projectOpt = ruleResolver.resolveActiveProject(apiKey);

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

        RateLimiter limiter = getStrategies().get(rule.getAlgorithm());
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
            return;
        }

        filterChain.doFilter(request, response);
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