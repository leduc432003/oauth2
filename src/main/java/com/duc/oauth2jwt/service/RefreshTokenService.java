package com.duc.oauth2jwt.service;

import com.duc.oauth2jwt.exception.AuthenticationException;
import com.duc.oauth2jwt.model.RefreshToken;
import com.duc.oauth2jwt.model.User;
import com.duc.oauth2jwt.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    @Value("${app.jwt.refresh-token-expiration}")
    private Long refreshTokenDurationMs;

    private final RefreshTokenRepository refreshTokenRepository;

    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    public RefreshToken createRefreshToken(User user) {
        // Delete existing token for this user if any
        refreshTokenRepository.deleteByUserId(user.getId());

        Instant expiryDate = Instant.now().plusMillis(refreshTokenDurationMs);
        Long ttlSeconds = refreshTokenDurationMs / 1000;

        RefreshToken refreshToken = RefreshToken.builder()
                .id(UUID.randomUUID().toString())
                .token(UUID.randomUUID().toString())
                .userId(user.getId())
                .userEmail(user.getEmail())
                .expiryDate(expiryDate)
                .timeToLive(ttlSeconds)
                .build();

        return refreshTokenRepository.save(refreshToken);
    }

    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpiryDate().isBefore(Instant.now())) {
            refreshTokenRepository.delete(token);
            throw new AuthenticationException("Refresh token was expired. Please make a new sign in request");
        }
        return token;
    }

    public void deleteByToken(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new AuthenticationException("Refresh token not found"));
        refreshTokenRepository.delete(refreshToken);
    }

    public void deleteByUserId(Long userId) {
        refreshTokenRepository.deleteByUserId(userId);
    }
}
