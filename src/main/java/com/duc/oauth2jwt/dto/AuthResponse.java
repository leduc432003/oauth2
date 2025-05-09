package com.duc.oauth2jwt.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response object containing authentication details and user information")
public class AuthResponse {

    @Schema(description = "JWT access token for authenticated requests", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String accessToken;

    @Schema(description = "Refresh token for obtaining a new access token", example = "dGhpc2lzYXJlZnJlc2h0b2tlbg...")
    private String refreshToken;

    @Schema(description = "Type of token, typically 'Bearer'", example = "Bearer", defaultValue = "Bearer")
    private String tokenType = "Bearer";

    @Schema(description = "Expiration time of the access token in milliseconds", example = "3600000")
    private Long expiresIn;

    @Schema(description = "Details of the authenticated user")
    private UserDto user;
}