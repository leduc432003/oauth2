package com.duc.oauth2jwt.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;
import org.springframework.data.redis.core.index.Indexed;

import java.io.Serializable;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@RedisHash("refresh_tokens")
public class RefreshToken implements Serializable {
    
    @Id
    private String id;

    @Indexed
    private String token;

    private Long userId;
    
    private String userEmail;

    private Instant expiryDate;
    
    @TimeToLive
    private Long timeToLive; // in seconds
}