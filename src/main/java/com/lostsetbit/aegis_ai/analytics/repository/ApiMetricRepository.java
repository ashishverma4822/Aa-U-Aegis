package com.lostsetbit.aegis_ai.analytics.repository;

import com.lostsetbit.aegis_ai.analytics.entity.ApiMetric;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ApiMetricRepository extends JpaRepository<ApiMetric, UUID> {
}