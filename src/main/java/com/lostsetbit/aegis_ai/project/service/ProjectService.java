package com.lostsetbit.aegis_ai.project.service;

import com.lostsetbit.aegis_ai.auth.entity.User;
import com.lostsetbit.aegis_ai.auth.repository.UserRepository;
import com.lostsetbit.aegis_ai.project.dto.CreateProjectRequest;
import com.lostsetbit.aegis_ai.project.dto.ProjectCreateResponse;
import com.lostsetbit.aegis_ai.project.dto.ProjectResponse;
import com.lostsetbit.aegis_ai.project.entity.Project;
import com.lostsetbit.aegis_ai.project.repository.ProjectRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private static final SecureRandom secureRandom = new SecureRandom();

    @Transactional
    public ProjectCreateResponse createProject(CreateProjectRequest request, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        String apiKey = "ag_live_" + UUID.randomUUID().toString().replace("-", "");
        String rawApiSecret = generateSecureSecret();
        String hashedSecret = passwordEncoder.encode(rawApiSecret);

        Project project = Project.builder()
                .name(request.getName())
                .description(request.getDescription())
                .apiKey(apiKey)
                .apiSecretHash(hashedSecret)
                .user(user)
                .build();

        Project savedProject = projectRepository.save(project);

        return ProjectCreateResponse.builder()
                .id(savedProject.getId())
                .name(savedProject.getName())
                .description(savedProject.getDescription())
                .apiKey(savedProject.getApiKey())
                .apiSecret(rawApiSecret)
                .createdAt(savedProject.getCreatedAt())
                .build();
    }

    @Transactional(readOnly = true)
    public List<ProjectResponse> getUserProjects(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        return projectRepository.findByUserId(user.getId())
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ProjectResponse getProjectById(UUID projectId, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        Project project = projectRepository.findByIdAndUserId(projectId, user.getId())
                .orElseThrow(() -> new EntityNotFoundException("Project not found or access denied"));

        return mapToResponse(project);
    }

    @Transactional
    public void deleteProject(UUID projectId, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        Project project = projectRepository.findByIdAndUserId(projectId, user.getId())
                .orElseThrow(() -> new EntityNotFoundException("Project not found or access denied"));

        projectRepository.delete(project);
    }

    private ProjectResponse mapToResponse(Project project) {
        return ProjectResponse.builder()
                .id(project.getId())
                .name(project.getName())
                .description(project.getDescription())
                .apiKey(project.getApiKey())
                .createdAt(project.getCreatedAt())
                .build();
    }

    private String generateSecureSecret() {
        byte[] randomBytes = new byte[32];
        secureRandom.nextBytes(randomBytes);
        return "sec_" + Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }
}