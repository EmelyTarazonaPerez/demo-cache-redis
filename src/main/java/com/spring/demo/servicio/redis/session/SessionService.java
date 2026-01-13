package com.spring.demo.servicio.redis.session;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

//session service
//guarda la session service en redis
//usa MAP
@Service
public class SessionService {
    private static final String PREFIX = "session:";

    private final RedisTemplate<String, String> redisTemplate;

    public SessionService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void saveSession(String sessionId, String userId) {

        String key = PREFIX + sessionId;

        redisTemplate.opsForHash().put(key, "userId", userId);
        redisTemplate.opsForHash().put(key, "createdAt", Instant.now().toString());
        redisTemplate.opsForHash().put(
                key,
                "expiresAt",
                Instant.now().plusSeconds(3600).toString()
        );

        redisTemplate.expire(key, Duration.ofHours(1));
    }

    public Long getUserIdFromSession(String sessionId) {
        String key = PREFIX + sessionId;
        String userId = (String) redisTemplate.opsForHash().get(key, "userId");
        return userId != null ? Long.valueOf(userId) : null;
    }

    public void deleteSession(String sessionId) {
        redisTemplate.delete(PREFIX + sessionId);
    }


}
