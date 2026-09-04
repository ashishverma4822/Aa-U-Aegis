package com.lostsetbit.aegis_ai.project.repository;

import com.lostsetbit.aegis_ai.project.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProjectRepository extends JpaRepository<Project, UUID> {

    List<Project> findByUserId(Long userId);

    Optional<Project> findByIdAndUserId(UUID id, Long userId);
}