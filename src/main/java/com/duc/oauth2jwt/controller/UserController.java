package com.duc.oauth2jwt.controller;

import com.duc.oauth2jwt.dto.UserDto;
import com.duc.oauth2jwt.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
@Tag(name = "User Controller", description = "API for user-related operations, restricted to users with USER role")
@SecurityRequirement(name = "Bearer Authentication") // Yêu cầu JWT token cho các endpoint
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    @PreAuthorize("hasRole('USER')")
    @Operation(
            summary = "Get current user",
            description = "Retrieves information of the currently authenticated user. Requires USER role."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved current user information"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
            @ApiResponse(responseCode = "403", description = "Forbidden - User does not have USER role")
    })
    public ResponseEntity<UserDto> getCurrentUser() {
        return ResponseEntity.ok(userService.getCurrentUser());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    @Operation(
            summary = "Get user by ID",
            description = "Retrieves information of a user by their ID. Requires USER role."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved user information"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
            @ApiResponse(responseCode = "403", description = "Forbidden - User does not have USER role"),
            @ApiResponse(responseCode = "404", description = "User with specified ID not found")
    })
    public ResponseEntity<UserDto> getUserById(
            @Parameter(description = "ID of the user to retrieve", required = true)
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(userService.getUserById(id));
    }
}