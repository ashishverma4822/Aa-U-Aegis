package com.lostsetbit.aegis_ai.analytics.service;

import com.lostsetbit.aegis_ai.analytics.entity.ApiMetric;
import com.lostsetbit.aegis_ai.analytics.event.TrafficMetricEvent;
import com.lostsetbit.aegis_ai.analytics.repository.ApiMetricRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TrafficMonitoringService {

    private final ApiMetricRepository apiMetricRepository;

    @Transactional
    public void recordMetric(TrafficMetricEvent event) {
        ApiMetric metric = ApiMetric.builder()
                .projectId(event.getProjectId())
                .endpoint(event.getEndpoint())
                .method(event.getMethod())
                .statusCode(event.getStatusCode())
                .responseTime(event.getResponseTime())
                .clientIdentifier(event.getClientIdentifier())
                .rateLimitStatus(event.getRateLimitStatus())
                .timestamp(event.getTimestamp())
                .build();

        apiMetricRepository.save(metric);
        log.debug("Persisted traffic metric for endpoint [{}] with status [{}]", event.getEndpoint(), event.getStatusCode());
    }
}