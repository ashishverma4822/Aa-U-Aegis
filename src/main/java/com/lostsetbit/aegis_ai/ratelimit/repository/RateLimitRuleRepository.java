package com.lostsetbit.aegis_ai.ratelimit.repository;

import com.lostsetbit.aegis_ai.ratelimit.entity.LimitType;
import com.lostsetbit.aegis_ai.ratelimit.entity.RateLimitRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RateLimitRuleRepository extends JpaRepository<RateLimitRule, Long> {

    List<RateLimitRule> findByProjectId(UUID projectId);

    boolean existsByProjectIdAndEndpointAndLimitType(UUID projectId, String endpoint, LimitType limitType);

    Optional<RateLimitRule> findByIdAndProjectUserId(Long id, Long userId);
}