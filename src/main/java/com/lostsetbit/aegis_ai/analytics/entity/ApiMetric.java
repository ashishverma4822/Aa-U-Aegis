package com.lostsetbit.aegis_ai.analytics.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "api_metrics")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiMetric {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "project_id", nullable = false)
    private UUID projectId;

    @Column(name = "endpoint", nullable = false)
    private String endpoint;

    @Column(name = "method", nullable = false, length = 10)
    private String method;

    @Column(name = "status_code", nullable = false)
    private Integer statusCode;

    @Column(name = "response_time", nullable = false)
    private Long responseTime;

    @Column(name = "client_identifier")
    private String clientIdentifier;

    @Enumerated(EnumType.STRING)
    @Column(name = "rate_limit_status", nullable = false)
    private RateLimitStatus rateLimitStatus;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;
}