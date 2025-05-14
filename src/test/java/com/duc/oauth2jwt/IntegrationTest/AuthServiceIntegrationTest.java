package com.duc.oauth2jwt.IntegrationTest;

import com.duc.oauth2jwt.dto.AuthRequest;
import com.duc.oauth2jwt.dto.AuthResponse;
import com.duc.oauth2jwt.dto.RegisterRequest;
import com.duc.oauth2jwt.exception.AuthenticationException;
import com.duc.oauth2jwt.model.Role;
import com.duc.oauth2jwt.model.User;
import com.duc.oauth2jwt.repository.RefreshTokenRepository;
import com.duc.oauth2jwt.repository.RoleRepository;
import com.duc.oauth2jwt.repository.UserRepository;
import com.duc.oauth2jwt.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.shaded.org.checkerframework.checker.units.qual.A;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
public class AuthServiceIntegrationTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @BeforeEach
    public void setup() {
        refreshTokenRepository.deleteAll();
        userRepository.deleteAll();

        // Ensure required roles exist in database
        if (!roleRepository.findByName(Role.RoleName.ROLE_USER).isPresent()) {
            Role userRole = new Role();
            userRole.setName(Role.RoleName.ROLE_USER);
            roleRepository.save(userRole);
        }

        if (!roleRepository.findByName(Role.RoleName.ROLE_ADMIN).isPresent()) {
            Role adminRole = new Role();
            adminRole.setName(Role.RoleName.ROLE_ADMIN);
            roleRepository.save(adminRole);
        }
    }

    @Test
    public void testRegister() {
        RegisterRequest request = new RegisterRequest();
        request.setName("Test Register");
        request.setEmail("register-test@example.com");
        request.setPassword("Password123");

        AuthResponse response = authService.register(request);

        assertNotNull(response);
        assertNotNull(response.getAccessToken());
        assertNotNull(response.getRefreshToken());
        assertEquals("register-test@example.com", response.getUser().getEmail());
        assertTrue(response.getUser().getRoles().contains("ROLE_USER"));

        // Verify the user was created in the database
        User savedUser = userRepository.findByEmail("register-test@example.com").orElse(null);
        assertNotNull(savedUser);
        assertEquals("Test Register", savedUser.getName());
    }

    @Test
    public void testRegisterWithExistingEmail() {
        // First registration
        RegisterRequest request = new RegisterRequest();
        request.setName("First User");
        request.setEmail("duplicate@example.com");
        request.setPassword("Password123");
        authService.register(request);

        // Second registration with same email
        RegisterRequest duplicateRequest = new RegisterRequest();
        duplicateRequest.setName("Second User");
        duplicateRequest.setEmail("duplicate@example.com");
        duplicateRequest.setPassword("Password456");

        assertThrows(AuthenticationException.class, () -> {
            authService.register(duplicateRequest);
        });
    }

    @Test
    public void testLogin() {
        // First register
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setName("Login Test");
        registerRequest.setEmail("login-service-test@example.com");
        registerRequest.setPassword("Password123");
        authService.register(registerRequest);

        // Then login
        AuthRequest loginRequest = new AuthRequest();
        loginRequest.setEmail("login-service-test@example.com");
        loginRequest.setPassword("Password123");

        AuthResponse response = authService.login(loginRequest);

        assertNotNull(response);
        assertNotNull(response.getAccessToken());
        assertNotNull(response.getRefreshToken());
        assertEquals("login-service-test@example.com", response.getUser().getEmail());
    }

    @Test
    public void testLoginWithInvalidCredentials() {
        // First register
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setName("Invalid Login Test");
        registerRequest.setEmail("invalid-login@example.com");
        registerRequest.setPassword("Password123");
        authService.register(registerRequest);

        // Then try login with wrong password
        AuthRequest loginRequest = new AuthRequest();
        loginRequest.setEmail("invalid-login@example.com");
        loginRequest.setPassword("WrongPassword");

        assertThrows(BadCredentialsException.class, () -> {
            authService.login(loginRequest);
        });
    }

    @Test
    public void testRefreshToken() {
        // First register
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setName("Refresh Test");
        registerRequest.setEmail("refresh-service-test@example.com");
        registerRequest.setPassword("Password123");
        AuthResponse registerResponse = authService.register(registerRequest);

        // Then refresh token
        String refreshToken = registerResponse.getRefreshToken();
        AuthResponse refreshResponse = authService.refreshToken(refreshToken);

        assertNotNull(refreshResponse);
        assertNotNull(refreshResponse.getAccessToken());
        assertEquals(refreshToken, refreshResponse.getRefreshToken());
        assertEquals("refresh-service-test@example.com", refreshResponse.getUser().getEmail());
    }

    @Test
    public void testRefreshTokenWithInvalidToken() {
        assertThrows(AuthenticationException.class, () -> {
            authService.refreshToken("invalid-token");
        });
    }

    @Test
    public void testLogoutAndRefresh() {
        // First register
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setName("Logout Test");
        registerRequest.setEmail("logout-service-test@example.com");
        registerRequest.setPassword("Password123");
        AuthResponse registerResponse = authService.register(registerRequest);

        String refreshToken = registerResponse.getRefreshToken();

        // Then logout
        authService.logout(refreshToken);

        // Try to refresh with the logged out token
        assertThrows(AuthenticationException.class, () -> {
            authService.refreshToken(refreshToken);
        });
    }
}
