package com.spring.demo.servicio.security;

import com.spring.demo.servicio.jwt.JwtService;
import com.spring.demo.servicio.redis.session.MultiSessionService;
import com.spring.demo.servicio.redis.session.SessionServiceJwt;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final SessionServiceJwt sessionService;
    private final UserDetailsService userDetailsService;
    private final MultiSessionService multiSessionService;

    public JwtAuthenticationFilter(
            JwtService jwtService,
            SessionServiceJwt sessionService,
            UserDetailsService userDetailsService,
            MultiSessionService multiSessionService) {
        this.jwtService = jwtService;
        this.sessionService = sessionService;
        this.userDetailsService = userDetailsService;
        this.multiSessionService = multiSessionService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);
        String jti = jwtService.extractJti(token);

        // 🔴 CONTROL DE SESIÓN
        if (!multiSessionService.isSessionValid(jti)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }


        String username = jwtService.obtenerClaims(token).getSubject();

        UserDetails userDetails =
                userDetailsService.loadUserByUsername(username);

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());

        SecurityContextHolder.getContext()
                .setAuthentication(authentication);

        filterChain.doFilter(request, response);
    }
}