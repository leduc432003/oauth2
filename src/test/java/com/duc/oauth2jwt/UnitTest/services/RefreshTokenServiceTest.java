package com.duc.oauth2jwt.UnitTest.services;

import com.duc.oauth2jwt.exception.AuthenticationException;
import com.duc.oauth2jwt.model.RefreshToken;
import com.duc.oauth2jwt.model.User;
import com.duc.oauth2jwt.repository.RefreshTokenRepository;
import com.duc.oauth2jwt.repository.UserRepository;
import com.duc.oauth2jwt.service.RefreshTokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class RefreshTokenServiceTest {
    @Mock
    private RefreshTokenRepository refreshTokenRepository;
    @Mock
    private UserRepository userRepository;
    @InjectMocks
    private RefreshTokenService refreshTokenService;
    private User testUser;
    private RefreshToken validRefreshToken;
    private RefreshToken expiredRefreshToken;

    @BeforeEach
    void setUp() {
        // Set refresh token expiration time
        ReflectionTestUtils.setField(refreshTokenService, "refreshTokenDurationMs", 86400000L);

        // Setup test user
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");

        // Setup valid refresh token
        validRefreshToken = new RefreshToken();
        validRefreshToken.setId(1L);
        validRefreshToken.setUser(testUser);
        validRefreshToken.setToken("valid-refresh-token");
        validRefreshToken.setExpiryDate(Instant.now().plusMillis(86400000));

        // Setup expired refresh token
        expiredRefreshToken = new RefreshToken();
        expiredRefreshToken.setId(2L);
        expiredRefreshToken.setUser(testUser);
        expiredRefreshToken.setToken("expired-refresh-token");
        expiredRefreshToken.setExpiryDate(Instant.now().minusMillis(86400000));
    }

    @Test
    void findByToken_Success() {
        // Arrange
        when(refreshTokenRepository.findByToken(anyString())).thenReturn(Optional.of(validRefreshToken));

        // Act
        Optional<RefreshToken> refreshToken = refreshTokenService.findByToken("valid-refresh-token");

        // Assert
        assertTrue(refreshToken.isPresent());
        assertEquals(validRefreshToken.getId(), refreshToken.get().getId());

        verify(refreshTokenRepository).findByToken("valid-refresh-token");
    }

    @Test
    void findByToken_NonExistent() {
        // Arrange
        when(refreshTokenRepository.findByToken(anyString())).thenReturn(Optional.empty());

        // Act
        Optional<RefreshToken> result = refreshTokenService.findByToken("non-existent-token");

        // Assert
        assertTrue(result.isEmpty());

        verify(refreshTokenRepository).findByToken("non-existent-token");
    }

    @Test
    void createRefreshToken_NewToken() {
        // Arrange
        when(refreshTokenRepository.findByUser(any(User.class))).thenReturn(Optional.empty());
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(invocation -> {
            RefreshToken token = invocation.getArgument(0);
            token.setId(1L);
            return token;
        });

        // Act
        RefreshToken result = refreshTokenService.createRefreshToken(testUser);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals(testUser, result.getUser());
        assertNotNull(result.getToken());
        assertNotNull(result.getExpiryDate());
        assertTrue(result.getExpiryDate().isAfter(Instant.now()));

        verify(refreshTokenRepository).findByUser(testUser);
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    void createRefreshToken_ReplaceExisting() {
        // Arrange
        when(refreshTokenRepository.findByUser(any(User.class))).thenReturn(Optional.of(validRefreshToken));
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(invocation -> {
            RefreshToken token = invocation.getArgument(0);
            token.setId(3L);
            return token;
        });

        // Act
        RefreshToken result = refreshTokenService.createRefreshToken(testUser);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals(testUser, result.getUser());
        assertNotNull(result.getToken());
        assertNotNull(result.getExpiryDate());

        verify(refreshTokenRepository).findByUser(testUser);
        verify(refreshTokenRepository).delete(validRefreshToken);
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    void verifyExpiration_Valid() {
        // Act
        RefreshToken result = refreshTokenService.verifyExpiration(validRefreshToken);

        // Assert
        assertNotNull(result);
        assertEquals(validRefreshToken.getId(), result.getId());

        verifyNoInteractions(refreshTokenRepository);
    }

    @Test
    void verifyExpiration_Expired() {
        // Act & Assert
        assertThrows(AuthenticationException.class, () -> refreshTokenService.verifyExpiration(expiredRefreshToken));

        verify(refreshTokenRepository).delete(expiredRefreshToken);
    }

    @Test
    void deleteByToken_Success() {
        // Arrange
        when(refreshTokenRepository.findByToken(anyString())).thenReturn(Optional.of(validRefreshToken));

        // Act
        refreshTokenService.deleteByToken("valid-refresh-token");

        // Assert
        verify(refreshTokenRepository).findByToken("valid-refresh-token");
        verify(refreshTokenRepository).delete(validRefreshToken);
    }

    @Test
    void deleteByToken_NonExistent() {
        // Arrange
        when(refreshTokenRepository.findByToken(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(AuthenticationException.class, () -> refreshTokenService.deleteByToken("non-existent-token"));

        verify(refreshTokenRepository).findByToken("non-existent-token");
        verify(refreshTokenRepository, never()).delete(any(RefreshToken.class));
    }

    @Test
    void deleteByUserId_Success() {
        // Arrange
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(testUser));

        // Act
        refreshTokenService.deleteByUserId(1L);

        // Assert
        verify(userRepository).findById(1L);
        verify(refreshTokenRepository).deleteByUser(testUser);
    }

    @Test
    void deleteByUserId_UserNotFound() {
        // Arrange
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(AuthenticationException.class, () -> refreshTokenService.deleteByUserId(999L));

        verify(userRepository).findById(999L);
        verifyNoInteractions(refreshTokenRepository);
    }
}
