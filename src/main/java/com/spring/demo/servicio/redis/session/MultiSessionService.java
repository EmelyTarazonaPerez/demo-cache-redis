package com.spring.demo.servicio.redis.session;

import com.spring.demo.model.DeviceInfo;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class MultiSessionService {

    private final RedisTemplate<String, String> redisTemplate;
    public MultiSessionService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void createSession(String userId, String jti, long jwtExpirationMillis, DeviceInfo deviceInfo) {
        long ttl = jwtExpirationMillis - System.currentTimeMillis();
        if (ttl <= 0) return;

        String sessionKey = "session:" + jti;
        String userSessionsKey = "user:sessions:" + userId;

        // 1️⃣ Guardar sesión como HASH (UN dispositivo)
        Map<String, String> sessionData = new HashMap<>();
        sessionData.put("userId", userId);
        sessionData.put("device", deviceInfo.device());
        sessionData.put("ip", deviceInfo.ip());
        sessionData.put("userAgent", deviceInfo.userAgent());
        sessionData.put("loginAt", deviceInfo.loginAt());

        redisTemplate.opsForHash().putAll(sessionKey, sessionData);
        redisTemplate.expire(sessionKey, ttl, TimeUnit.MILLISECONDS);

        // 2️⃣ Registrar JTI en el SET del usuario
        redisTemplate.opsForSet().add(userSessionsKey, jti);
        redisTemplate.expire(userSessionsKey, ttl, TimeUnit.MILLISECONDS);
    }

    public boolean isSessionValid(String jti) {
        return redisTemplate.hasKey("session:" + jti);
    }

    public void logout(String userId, String jti) {
        // Eliminar sesión individual
        redisTemplate.delete("session:" + jti);
        // Quitar esa sesión del usuario
        redisTemplate.opsForSet().remove("user:sessions:" + userId, jti);
    }

    public void logoutAll(String userId) {
        String key = "user:sessions:" + userId;
        Set<String> sessions = redisTemplate.opsForSet().members(key);

        if (sessions != null) {
            for (String jti : sessions) {
                redisTemplate.delete("session:" + jti);
            }
        }
        redisTemplate.delete(key);
    }

    public List<Map<Object, Object>> getActiveDevices(String userId) {

        Set<String> sessions = redisTemplate.opsForSet()
                .members("user:sessions:" + userId);

        if (sessions == null) return List.of();

        List<Map<Object, Object>> devices = new ArrayList<>();

        for (String jti : sessions) {
            Map<Object, Object> data =
                    redisTemplate.opsForHash().entries("session:" + jti);

            if (!data.isEmpty()) {
                devices.add(data);
            }
        }

        return devices;
    }


}
