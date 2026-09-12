package com.lostsetbit.aegis_ai.analytics.listener;

import com.lostsetbit.aegis_ai.analytics.event.TrafficMetricEvent;
import com.lostsetbit.aegis_ai.analytics.service.TrafficMonitoringService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TrafficMetricEventListener {

    private final TrafficMonitoringService trafficMonitoringService;

    @Async
    @EventListener
    public void handleTrafficMetricEvent(TrafficMetricEvent event) {
        try {
            trafficMonitoringService.recordMetric(event);
        } catch (Exception e) {
            log.error("Failed to process async traffic metric event for project [{}]", event.getProjectId(), e);
        }
    }
}