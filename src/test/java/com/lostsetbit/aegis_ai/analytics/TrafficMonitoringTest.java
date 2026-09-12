package com.lostsetbit.aegis_ai.analytics;

import com.lostsetbit.aegis_ai.analytics.entity.ApiMetric;
import com.lostsetbit.aegis_ai.analytics.entity.RateLimitStatus;
import com.lostsetbit.aegis_ai.analytics.repository.ApiMetricRepository;
import com.lostsetbit.aegis_ai.auth.entity.Role;
import com.lostsetbit.aegis_ai.auth.entity.User;
import com.lostsetbit.aegis_ai.auth.repository.UserRepository;
import com.lostsetbit.aegis_ai.project.entity.Project;
import com.lostsetbit.aegis_ai.project.repository.ProjectRepository;
import com.lostsetbit.aegis_ai.ratelimit.entity.LimitType;
import com.lostsetbit.aegis_ai.ratelimit.entity.RateLimitAlgorithm;
import com.lostsetbit.aegis_ai.ratelimit.entity.RateLimitRule;
import com.lostsetbit.aegis_ai.ratelimit.repository.RateLimitRuleRepository;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class TrafficMonitoringTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private RateLimitRuleRepository ruleRepository;

    @Autowired
    private ApiMetricRepository apiMetricRepository;

    @Autowired
    private UserRepository userRepository;

    private Project testProject;

    @BeforeEach
    void setUp() {
        apiMetricRepository.deleteAll();
        ruleRepository.deleteAll();
        projectRepository.deleteAll();
        userRepository.deleteAll();

        User testUser = User.builder()
                .name("Test User")
                .email("test@example.com")
                .passwordHash("hashed_password_123")
                .role(Role.USER)
                .build();
        testUser = userRepository.save(testUser);

        testProject = Project.builder()
                .name("Analytics Test App")
                .apiKey("ag_live_analytics_test_key_12345")
                .apiSecretHash("dummy_hashed_secret_for_test")
                .user(testUser)
                .isActive(true)
                .build();
        testProject = projectRepository.save(testProject);

        RateLimitRule rule = RateLimitRule.builder()
                .project(testProject)
                .endpoint("/api/demo/products")
                .requestLimit(1)
                .timeWindow(60)
                .limitType(LimitType.API_KEY)
                .algorithm(RateLimitAlgorithm.TOKEN_BUCKET)
                .build();
        ruleRepository.save(rule);
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = {"USER"})
    void testAllowedAndRateLimitedMetricsArePersistedAsync() throws Exception {
        mockMvc.perform(get("/api/demo/products")
                        .header("X-RateGuard-API-Key", testProject.getApiKey()))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/demo/products")
                        .header("X-RateGuard-API-Key", testProject.getApiKey()))
                .andExpect(status().isTooManyRequests());

        Awaitility.await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            List<ApiMetric> metrics = apiMetricRepository.findAll();
            assertThat(metrics).hasSize(2);

            ApiMetric allowedMetric = metrics.stream()
                    .filter(m -> m.getRateLimitStatus() == RateLimitStatus.ALLOWED)
                    .findFirst()
                    .orElseThrow();
            assertThat(allowedMetric.getStatusCode()).isEqualTo(200);
            assertThat(allowedMetric.getEndpoint()).isEqualTo("/api/demo/products");
            assertThat(allowedMetric.getProjectId()).isEqualTo(testProject.getId());

            ApiMetric rateLimitedMetric = metrics.stream()
                    .filter(m -> m.getRateLimitStatus() == RateLimitStatus.RATE_LIMITED)
                    .findFirst()
                    .orElseThrow();
            assertThat(rateLimitedMetric.getStatusCode()).isEqualTo(429);
            assertThat(rateLimitedMetric.getRateLimitStatus()).isEqualTo(RateLimitStatus.RATE_LIMITED);
            assertThat(rateLimitedMetric.getProjectId()).isEqualTo(testProject.getId());
        });
    }
}