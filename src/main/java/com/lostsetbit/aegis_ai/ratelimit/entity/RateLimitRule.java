package com.lostsetbit.aegis_ai.ratelimit.entity;

import com.lostsetbit.aegis_ai.project.entity.Project;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "rate_limit_rules",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_project_endpoint_limittype",
                        columnNames = {"project_id", "endpoint", "limit_type"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RateLimitRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(nullable = false)
    private String endpoint;

    @Enumerated(EnumType.STRING)
    @Column(name = "limit_type", nullable = false)
    private LimitType limitType;

    @Column(name = "request_limit", nullable = false)
    private Integer requestLimit;

    @Column(name = "time_window", nullable = false)
    private Integer timeWindow; // in seconds

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RateLimitAlgorithm algorithm;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}