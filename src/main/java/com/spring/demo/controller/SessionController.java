package com.spring.demo.controller;

import com.spring.demo.servicio.redis.session.SessionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("session")
public class SessionController {

    private final SessionService sessionService;

    public SessionController(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    // 🔐 LOGIN (crear sesión)
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestParam String userId) {

        String sessionId = UUID.randomUUID().toString();
        sessionService.saveSession(sessionId, userId);

        return ResponseEntity.ok(
                Map.of(
                        "sessionId", sessionId,
                        "message", "Sesión creada correctamente"
                )
        );
    }

    // 👤 INFO USUARIO (sesión válida)
    @GetMapping("/me")
    public ResponseEntity<?> me(
            @RequestHeader("X-SESSION-ID") String sessionId
    ) {

        Long userId = sessionService.getUserIdFromSession(sessionId);

        if (userId == null) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Sesión inválida o expirada");
        }

        return ResponseEntity.ok(
                Map.of(
                        "userId", userId,
                        "message", "Sesión activa"
                )
        );
    }

    // 🚪 LOGOUT (eliminar sesión)
    @DeleteMapping("/logout")
    public ResponseEntity<?> logout(
            @RequestHeader("X-SESSION-ID") String sessionId
    ) {

        sessionService.deleteSession(sessionId);

        return ResponseEntity.ok("Sesión cerrada correctamente");
    }
}
