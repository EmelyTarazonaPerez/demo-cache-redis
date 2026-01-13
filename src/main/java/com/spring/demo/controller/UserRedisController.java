package com.spring.demo.controller;

import com.spring.demo.model.UserHash;
import com.spring.demo.servicio.redis.BD.UserServiceRedis;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/redis/users")
public class UserRedisController {
    private final UserServiceRedis userServiceRedis;

    public UserRedisController(UserServiceRedis userServiceRedis) {
        this.userServiceRedis = userServiceRedis;
    }

    @PostMapping
    public ResponseEntity<Void> save(@RequestBody UserHash user) {
        userServiceRedis.save(user);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserHash> get(@PathVariable String id) {
        UserHash user = userServiceRedis.getUser(id);

        if (user == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(user);
    }

    @PatchMapping("/{id}/age")
    public ResponseEntity<Void> updateAge(
            @PathVariable String id,
            @RequestParam Integer age) {

        userServiceRedis.updateAge(id, age);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        userServiceRedis.delete(id);
        return ResponseEntity.noContent().build();
    }
}
