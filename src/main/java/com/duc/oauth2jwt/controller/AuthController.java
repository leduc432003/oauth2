package com.duc.oauth2jwt.controller;

import com.duc.oauth2jwt.dto.AuthRequest;
import com.duc.oauth2jwt.dto.AuthResponse;
import com.duc.oauth2jwt.dto.RegisterRequest;
import com.duc.oauth2jwt.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Auth Controller", description = "API for user authentication and authorization operations")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(
            summary = "User login",
            description = "Authenticates a user with email and password, returning an access token and refresh token."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully authenticated user, returns access and refresh tokens"),
            @ApiResponse(responseCode = "400", description = "Invalid request body or credentials"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid email or password")
    })
    public ResponseEntity<AuthResponse> login(
            @Parameter(description = "Login request containing email and password", required = true)
            @Valid @RequestBody AuthRequest loginRequest
    ) {
        return ResponseEntity.ok(authService.login(loginRequest));
    }

    @PostMapping("/register")
    @Operation(
            summary = "User registration",
            description = "Registers a new user with name, email, and password, returning an access token and refresh token."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully registered user, returns access and refresh tokens"),
            @ApiResponse(responseCode = "400", description = "Invalid request body or email already exists"),
            @ApiResponse(responseCode = "409", description = "Conflict - Email already registered")
    })
    public ResponseEntity<AuthResponse> register(
            @Parameter(description = "Registration request containing name, email, and password", required = true)
            @Valid @RequestBody RegisterRequest registerRequest
    ) {
        return ResponseEntity.ok(authService.register(registerRequest));
    }

    @PostMapping("/refresh")
    @Operation(
            summary = "Refresh access token",
            description = "Generates a new access token using a valid refresh token."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully refreshed access token"),
            @ApiResponse(responseCode = "400", description = "Invalid or missing refresh token"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or expired refresh token")
    })
    public ResponseEntity<AuthResponse> refreshToken(
            @Parameter(description = "Refresh token used to generate a new access token", required = true)
            @RequestParam String refreshToken
    ) {
        return ResponseEntity.ok(authService.refreshToken(refreshToken));
    }

    @PostMapping("/logout")
    @Operation(
            summary = "User logout",
            description = "Invalidates the provided refresh token to log out the user."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully logged out"),
            @ApiResponse(responseCode = "400", description = "Invalid or missing refresh token"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid refresh token")
    })
    public ResponseEntity<Void> logout(
            @Parameter(description = "Refresh token to invalidate for logout", required = true)
            @RequestParam String refreshToken
    ) {
        authService.logout(refreshToken);
        return ResponseEntity.ok().build();
    }
}