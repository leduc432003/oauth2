package com.duc.oauth2jwt.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request object for user authentication")
public class AuthRequest {

    @NotBlank
    @Email
    @Schema(description = "Email address of the user", example = "john.doe@example.com", required = true)
    private String email;

    @NotBlank
    @Schema(description = "Password for the user account", example = "securePassword123", required = true)
    private String password;
}