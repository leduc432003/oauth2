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

import java.util.List;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@Tag(name = "Admin Controller", description = "API for administrative operations, restricted to users with ADMIN role")
@SecurityRequirement(name = "Bearer Authentication") // Yêu cầu JWT token cho các endpoint
public class AdminController {

    private final UserService userService;

    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Get all users",
            description = "Retrieves a list of all registered users in the system. Only accessible to users with ADMIN role."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list of users"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
            @ApiResponse(responseCode = "403", description = "Forbidden - User does not have ADMIN role")
    })
    public ResponseEntity<List<UserDto>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @PutMapping("/users/{id}/give-admin")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Grant admin role to a user",
            description = "Grants the ADMIN role to the user with the specified ID. Only accessible to users with ADMIN role."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully granted ADMIN role to the user"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
            @ApiResponse(responseCode = "403", description = "Forbidden - User does not have ADMIN role"),
            @ApiResponse(responseCode = "404", description = "User with specified ID not found")
    })
    public ResponseEntity<UserDto> giveAdminRole(
            @Parameter(description = "ID of the user to grant ADMIN role", required = true)
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(userService.giveAdminRole(id));
    }

    @PutMapping("/users/{id}/remove-admin")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Remove admin role from a user",
            description = "Removes the ADMIN role from the user with the specified ID. Only accessible to users with ADMIN role."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully removed ADMIN role from the user"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
            @ApiResponse(responseCode = "403", description = "Forbidden - User does not have ADMIN role"),
            @ApiResponse(responseCode = "404", description = "User with specified ID not found")
    })
    public ResponseEntity<UserDto> removeAdminRole(
            @Parameter(description = "ID of the user to remove ADMIN role", required = true)
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(userService.removeAdminRole(id));
    }
}