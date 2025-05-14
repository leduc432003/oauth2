package com.duc.oauth2jwt.UnitTest.services;

import com.duc.oauth2jwt.dto.AuthRequest;
import com.duc.oauth2jwt.dto.AuthResponse;
import com.duc.oauth2jwt.dto.RegisterRequest;
import com.duc.oauth2jwt.exception.AuthenticationException;
import com.duc.oauth2jwt.model.RefreshToken;
import com.duc.oauth2jwt.model.Role;
import com.duc.oauth2jwt.model.User;
import com.duc.oauth2jwt.repository.RoleRepository;
import com.duc.oauth2jwt.repository.UserRepository;
import com.duc.oauth2jwt.security.JwtTokenProvider;
import com.duc.oauth2jwt.service.AuthService;
import com.duc.oauth2jwt.service.RefreshTokenService;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Collections;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private UserRepository userRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtTokenProvider tokenProvider;
    @Mock
    private RefreshTokenService refreshTokenService;
    @Mock
    private Authentication authentication;
    @InjectMocks
    private AuthService authService;

    private User testUser;
    private Role userRole;
    private RefreshToken refreshToken;

    @BeforeEach
    void setUp() {
        // Setup test user
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setName("Test User");
        testUser.setPassword("encodedPassword");
        testUser.setProvider(User.AuthProvider.LOCAL);

        // Setup role
        userRole = new Role();
        userRole.setId(1L);
        userRole.setName(Role.RoleName.ROLE_USER);

        testUser.setRoles(Collections.singleton(userRole));

        // Setup refresh token
        refreshToken = new RefreshToken();
        refreshToken.setId(1L);
        refreshToken.setUser(testUser);
        refreshToken.setToken("refresh-token");
        refreshToken.setExpiryDate(Instant.now().plusMillis(86400000));
    }

    @Test
    void login_Success() {
        // Arrange
        AuthRequest loginRequest = new AuthRequest();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("password");

       when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
       when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(testUser));
       when(tokenProvider.generateToken(any(Authentication.class))).thenReturn("access-token");
       when(tokenProvider.getJwtExpirationInMs()).thenReturn(3600000L);
       when(refreshTokenService.createRefreshToken(any(User.class))).thenReturn(refreshToken);

        // Act
        AuthResponse response = authService.login(loginRequest);

        // Assert
        assertNotNull(response);
        assertEquals("access-token", response.getAccessToken());
        assertEquals("refresh-token", response.getRefreshToken());
        assertEquals("Bearer", response.getTokenType());
        assertEquals(3600000L, response.getExpiresIn());
        assertNotNull(response.getUser());
        assertEquals(testUser.getId(), response.getUser().getId());
        assertEquals(testUser.getEmail(), response.getUser().getEmail());
        assertEquals(testUser.getName(), response.getUser().getName());

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository).findByEmail(loginRequest.getEmail());
        verify(tokenProvider).generateToken(authentication);
        verify(refreshTokenService).createRefreshToken(testUser);
    }

    @Test
    void login_UserNotFound() {
        // Arrange
        AuthRequest loginRequest = new AuthRequest();
        loginRequest.setEmail("nonexistent@example.com");
        loginRequest.setPassword("password");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(AuthenticationException.class, () -> authService.login(loginRequest));

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository).findByEmail(loginRequest.getEmail());
        verifyNoInteractions(tokenProvider);
        verifyNoInteractions(refreshTokenService);
    }

    @Test
    void register_Success() {
        // Arrange
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setName("New User");
        registerRequest.setEmail("new@example.com");
        registerRequest.setPassword("password");

        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(roleRepository.findByName(any(Role.RoleName.class))).thenReturn(Optional.of(userRole));
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(tokenProvider.generateToken(any(Authentication.class))).thenReturn("access-token");
        when(tokenProvider.getJwtExpirationInMs()).thenReturn(3600000L);
        when(refreshTokenService.createRefreshToken(any(User.class))).thenReturn(refreshToken);

        // Act
        AuthResponse response = authService.register(registerRequest);

        // Assert
        assertNotNull(response);
        assertEquals("access-token", response.getAccessToken());
        assertEquals("refresh-token", response.getRefreshToken());

        verify(userRepository).existsByEmail(registerRequest.getEmail());
        verify(roleRepository).findByName(Role.RoleName.ROLE_USER);
        verify(passwordEncoder).encode(registerRequest.getPassword());
        verify(userRepository).save(any(User.class));
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(tokenProvider).generateToken(authentication);
        verify(refreshTokenService).createRefreshToken(any(User.class));
    }

    @Test
    void register_EmailAlreadyInUse() {
        // Arrange
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setName("New User");
        registerRequest.setEmail("existing@example.com");
        registerRequest.setPassword("password");

        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        // Act & Assert
        assertThrows(AuthenticationException.class, () -> authService.register(registerRequest));

        verify(userRepository).existsByEmail(registerRequest.getEmail());
        verifyNoInteractions(roleRepository);
        verifyNoInteractions(passwordEncoder);
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void refreshToken_Success() {
        // Arrange
        String refreshTokenString = "refresh-token";

        when(refreshTokenService.findByToken(anyString())).thenReturn(Optional.of(refreshToken));
        when(refreshTokenService.verifyExpiration(any(RefreshToken.class))).thenReturn(refreshToken);
        when(tokenProvider.generateTokenFromUsername(anyString())).thenReturn("new-access-token");
        when(tokenProvider.getJwtExpirationInMs()).thenReturn(3600000L);

        // Act
        AuthResponse response = authService.refreshToken(refreshTokenString);

        // Assert
        assertNotNull(response);
        assertEquals("new-access-token", response.getAccessToken());
        assertEquals(refreshTokenString, response.getRefreshToken());

        verify(refreshTokenService).findByToken(refreshTokenString);
        verify(refreshTokenService).verifyExpiration(refreshToken);
        verify(tokenProvider).generateTokenFromUsername(testUser.getEmail());
    }

    @Test
    void refreshToken_InvalidToken() {
        // Arrange
        String invalidToken = "invalid-token";

        when(refreshTokenService.findByToken(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(AuthenticationException.class, () -> authService.refreshToken(invalidToken));

        verify(refreshTokenService).findByToken(invalidToken);
        verifyNoMoreInteractions(refreshTokenService);
        verifyNoInteractions(tokenProvider);
    }

    @Test
    void logout_Success() {
        // Arrange
        String refreshTokenString = "refresh-token";

        // Act
        authService.logout(refreshTokenString);

        // Assert
        verify(refreshTokenService).deleteByToken(refreshTokenString);
    }
}
