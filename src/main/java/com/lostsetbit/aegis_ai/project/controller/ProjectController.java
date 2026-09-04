package com.lostsetbit.aegis_ai.project.controller;

import com.lostsetbit.aegis_ai.project.dto.CreateProjectRequest;
import com.lostsetbit.aegis_ai.project.dto.ProjectCreateResponse;
import com.lostsetbit.aegis_ai.project.dto.ProjectResponse;
import com.lostsetbit.aegis_ai.project.service.ProjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @PostMapping
    public ResponseEntity<ProjectCreateResponse> createProject(
            @Valid @RequestBody CreateProjectRequest request,
            Principal principal) {
        ProjectCreateResponse response = projectService.createProject(request, principal.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<ProjectResponse>> getUserProjects(Principal principal) {
        List<ProjectResponse> projects = projectService.getUserProjects(principal.getName());
        return ResponseEntity.ok(projects);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProjectResponse> getProjectById(
            @PathVariable UUID id,
            Principal principal) {
        ProjectResponse project = projectService.getProjectById(id, principal.getName());
        return ResponseEntity.ok(project);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProject(
            @PathVariable UUID id,
            Principal principal) {
        projectService.deleteProject(id, principal.getName());
        return ResponseEntity.noContent().build();
    }
}