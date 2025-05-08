package com.duc.oauth2jwt.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JwtConfig {
    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.access-token-expiration}")
    private long jwtExpirationInMs;

    @Value("${app.jwt.refresh-token-expiration}")
    private long refreshTokenExpirationInMs;

    public String getJwtSecret() {
        return jwtSecret;
    }

    public long getJwtExpirationInMs() {
        return jwtExpirationInMs;
    }

    public long getRefreshTokenExpirationInMs() {
        return refreshTokenExpirationInMs;
    }
}
