package com.spring.demo.controller;

import com.spring.demo.model.DeviceInfo;
import com.spring.demo.model.LoginRequest;
import com.spring.demo.servicio.jwt.JwtService;
import com.spring.demo.servicio.redis.session.MultiSessionService;
import com.spring.demo.servicio.redis.session.SessionServiceJwt;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final SessionServiceJwt sessionService;
    private final MultiSessionService multiSessionService;

    public AuthController(
            AuthenticationManager authenticationManager,
            JwtService jwtService,
            SessionServiceJwt sessionService,
            MultiSessionService multiSessionService

    ) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.sessionService = sessionService;
        this.multiSessionService = multiSessionService;
    }

    //Enpoind utilizados para unicasesion en un solo dispositivo
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.username(),
                        request.password()
                )
        );

        String token = jwtService.generarToken(request.username());

        Claims claims = jwtService.obtenerClaims(token);
        String jti = claims.getId();

        sessionService.guardarSesion(jti, request.username());

        return ResponseEntity.ok(Map.of("token", token));
    }

    @DeleteMapping("/logout")
    public ResponseEntity<String> logout (HttpServletRequest request) {


        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.badRequest().body("No token provided");
        }

        String token = authHeader.substring(7); // quita "Bearer "
        String jti = jwtService.extractJti(token);

        sessionService.eliminarSesion(jti);

        return ResponseEntity.ok("Session close");
    }

    //Enpoind utilizados para multiples secciones
    @PostMapping("/login-user")
    public ResponseEntity<?> loginMulti(@RequestBody LoginRequest request, HttpServletRequest agent){
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.username(),
                        request.password()
                )
        );

        String token = jwtService.generarToken(request.username());

        Claims claims = jwtService.obtenerClaims(token);
        String jti = claims.getId();
        String userId = claims.getSubject();
        Date expiration = claims.getExpiration();
        long jwtExpirationMillis = expiration.getTime();

        DeviceInfo deviceInfo = new DeviceInfo(
                detectDevice(agent.getHeader("User-Agent")),
                agent.getRemoteAddr(),
                agent.getHeader("User-Agent"),
                LocalDateTime.now().toString()
        );
        multiSessionService.createSession(userId, jti, jwtExpirationMillis, deviceInfo );

        return ResponseEntity.ok(Map.of("token", token));
    }

    @DeleteMapping("/logout-ona")
    public ResponseEntity<String> logoutSession(HttpServletRequest request) {

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.badRequest().body("No token provided");
        }

        String token = authHeader.substring(7);
        String jti = jwtService.extractJti(token);
        String userId = jwtService.obtenerClaims(token).getSubject();

        multiSessionService.logout(userId, jti);

        return ResponseEntity.ok("Session closed");
    }

    private String detectDevice(String userAgent) {
        if (userAgent == null) return "UNKNOWN";
        if (userAgent.contains("Android")) return "Android";
        if (userAgent.contains("iPhone")) return "iPhone";
        if (userAgent.contains("Windows")) return "Windows";
        if (userAgent.contains("Mac")) return "Mac";

        return "Other";
    }


    @DeleteMapping("/logout-all")
    public ResponseEntity<String> logoutAllSessions(HttpServletRequest request) {

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.badRequest().body("No token provided");
        }

        String token = authHeader.substring(7);
        String userId = jwtService.obtenerClaims(token).getSubject();

        multiSessionService.logoutAll(userId);

        return ResponseEntity.ok("All sessions closed");
    }

    @GetMapping("/list-device")
    public List<Map<Object, Object>> listDevice(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return (List<Map<Object, Object>>) ResponseEntity.badRequest().body("No token provided");
        }

        String token = authHeader.substring(7);
        String userId = jwtService.obtenerClaims(token).getSubject();

        return multiSessionService.getActiveDevices(userId);

    }

}