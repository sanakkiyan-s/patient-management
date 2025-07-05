package com.sana.authservice.Redis;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class RefreshTokenService {
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String REFRESH_PREFIX = "refresh:";
    private static final String BLACKLIST_PREFIX = "blacklist:";

    @Autowired
    public RefreshTokenService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void saveRefreshToken(String token, String username, Instant expiry) {
        try {
            long ttl = Duration.between(Instant.now(), expiry).getSeconds();
            redisTemplate.opsForValue().set(REFRESH_PREFIX + token, username, ttl, TimeUnit.SECONDS);
            log.info("Saved refresh token for user: {}", username);
        } catch (Exception e) {
            log.error("Failed to save refresh token", e);
            throw new RuntimeException("Failed to save refresh token");
        }
    }

    public boolean isTokenValid(String token) {
        try {
            return Boolean.TRUE.equals(redisTemplate.hasKey(REFRESH_PREFIX + token));
        } catch (Exception e) {
            log.error("Failed to validate token", e);
            return false;
        }
    }

    public void deleteByToken(String token) {
        try {
            redisTemplate.delete(REFRESH_PREFIX + token);
            log.info("Deleted refresh token");
        } catch (Exception e) {
            log.error("Failed to delete refresh token", e);
            throw new RuntimeException("Failed to delete refresh token");
        }
    }

    public void blacklistAccessToken(String token, Date expiry) {
        try {
            long ttl = Duration.between(Instant.now(), expiry.toInstant()).getSeconds();
            if (ttl <= 0) ttl = 1;
            redisTemplate.opsForValue().set(BLACKLIST_PREFIX + token, "true", ttl, TimeUnit.SECONDS);
            log.info("Blacklisted access token");
        } catch (Exception e) {
            log.error("Failed to blacklist token", e);
            throw new RuntimeException("Failed to blacklist token");
        }
    }

    public boolean isAccessTokenBlacklisted(String token) {
        try {
            return Boolean.TRUE.equals(redisTemplate.hasKey(BLACKLIST_PREFIX + token));
        } catch (Exception e) {
            log.error("Failed to check token blacklist", e);
            return false;
        }
    }
}
