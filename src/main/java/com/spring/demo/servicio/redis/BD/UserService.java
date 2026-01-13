package com.spring.demo.servicio.redis.BD;

import com.spring.demo.model.User;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

//no guarda la session service en docker
//solo usa cacheable parte de spring
@Service
public class UserService {
    @Cacheable(value = "users", key = "#id")
    public User getUserById(Long id) {
        System.out.println("Consultando usuario desde 'base de datos'...");
        simulateSlowService();
        return new User(id, "Usuario " + id);
    }

    private void simulateSlowService() {
        try {
            Thread.sleep(3000); // Simula consulta lenta
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
