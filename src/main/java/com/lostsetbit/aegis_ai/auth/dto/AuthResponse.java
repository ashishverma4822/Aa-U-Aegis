package com.lostsetbit.aegis_ai.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class AuthResponse {
    private String accessToken;

    @Builder.Default
    private String tokenType = "Bearer";
    private UserResponse user;
}
