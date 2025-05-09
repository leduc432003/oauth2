package com.duc.oauth2jwt.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Data transfer object representing user information")
public class UserDto {

    @Schema(description = "Unique identifier of the user", example = "1")
    private Long id;

    @Schema(description = "Email address of the user", example = "john.doe@example.com")
    private String email;

    @Schema(description = "Full name of the user", example = "John Doe")
    private String name;

    @Schema(description = "URL of the user's profile image, if available", example = "https://example.com/images/user.jpg")
    private String imageUrl;

    @Schema(description = "Set of roles assigned to the user", example = "[\"USER\", \"ADMIN\"]")
    private Set<String> roles;
}