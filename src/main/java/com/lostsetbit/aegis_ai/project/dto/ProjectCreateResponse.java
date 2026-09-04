package com.lostsetbit.aegis_ai.project.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class ProjectCreateResponse {
    private UUID id;
    private String name;
    private String description;
    private String apiKey;
    private String apiSecret;
    private LocalDateTime createdAt;
}
