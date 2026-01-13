package com.spring.demo.servicio.redis.session;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

//Servicio de session redis
//guarda la session en docker
//usa JWT guarda el TTL
@Service
public class SessionServiceJwt {
    private final RedisTemplate<String, String> redisTemplate;

    public SessionServiceJwt(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void guardarSesion(String jti, String username) {
        redisTemplate.opsForValue()
                .set(
                        "session:" + jti,
                        username,
                        1,
                        TimeUnit.HOURS
                );
    }

    public boolean existeSesion(String jti) {
        return redisTemplate.hasKey("session:" + jti);
    }

    public void eliminarSesion(String jti) {
        redisTemplate.delete("session:" + jti);
    }

}
