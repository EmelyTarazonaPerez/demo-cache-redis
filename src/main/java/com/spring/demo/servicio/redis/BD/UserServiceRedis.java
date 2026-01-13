package com.spring.demo.servicio.redis.BD;

import com.spring.demo.model.UserHash;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;

//servicio completo para usar redis como BD
@Service
public class UserServiceRedis {
    private final RedisTemplate<String, Object> redisTemplate;

    public UserServiceRedis(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }
    private String key(String id) {
        return "user:" + id;
    }

    public void save(UserHash user) {
        String redisKey = key(user.getId());

        redisTemplate.opsForHash().put(redisKey, "id", user.getId());
        redisTemplate.opsForHash().put(redisKey, "name", user.getName());
        redisTemplate.opsForHash().put(redisKey, "email", user.getEmail());
        redisTemplate.opsForHash().put(redisKey, "age", user.getAge());

        redisTemplate.expire(redisKey, Duration.ofMinutes(10));
    }

    public UserHash getUser(String id) {
        String redisKey = key(id);
        if (!redisTemplate.hasKey(redisKey)) {
            return null;
        }
        Map<Object, Object> entries = redisTemplate.opsForHash().entries(redisKey);

        UserHash user = new UserHash();
        user.setId((String) entries.get("id"));
        user.setName((String) entries.get("name"));
        user.setEmail((String) entries.get("email"));
        user.setAge((Integer) entries.get("age"));

        return user;
    }

    public void updateAge(String id, Integer age) {
        redisTemplate.opsForHash().put(key(id), "age", age);
    }

    public void delete(String id) {
        redisTemplate.delete(key(id));
    }
}
