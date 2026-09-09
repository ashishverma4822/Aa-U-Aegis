package com.lostsetbit.aegis_ai.ratelimit.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckRateLimitRequest {

    @NotBlank(message = "Endpoint is required")
    private String endpoint;

    private String clientIdentifier;
}
