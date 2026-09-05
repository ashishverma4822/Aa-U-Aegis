package com.lostsetbit.aegis_ai.ratelimit.dto;

import com.lostsetbit.aegis_ai.ratelimit.entity.LimitType;
import com.lostsetbit.aegis_ai.ratelimit.entity.RateLimitAlgorithm;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateRateLimitRuleRequest {

    @NotBlank(message = "Endpoint cannot be blank")
    @Pattern(regexp = "^/.*", message = "Endpoint must start with a forward slash ('/')")
    private String endpoint;

    @NotNull(message = "Limit type is required")
    private LimitType limitType;

    @NotNull(message = "Request limit is required")
    @Min(value = 1, message = "Request limit must be greater than 0")
    private Integer requestLimit;

    @NotNull(message = "Time window is required")
    @Min(value = 1, message = "Time window must be greater than 0")
    private Integer timeWindow;

    @NotNull(message = "Algorithm is required")
    private RateLimitAlgorithm algorithm;
}

