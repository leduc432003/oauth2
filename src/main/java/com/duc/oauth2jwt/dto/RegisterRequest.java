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
@Schema(description = "Request object for registering a new user")
public class RegisterRequest {

    @NotBlank
    @Size(min = 3, max = 50)
    @Schema(description = "Full name of the user", example = "John Doe", required = true)
    private String name;

    @NotBlank
    @Size(max = 100)
    @Email
    @Schema(description = "Email address of the user", example = "john.doe@example.com", required = true)
    private String email;

    @NotBlank
    @Size(min = 6, max = 40)
    @Schema(description = "Password for the user account", example = "securePassword123", required = true)
    private String password;
}