package com.sana.patientservice.SecurityConfig;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
        throws ServletException, IOException {
        
        try {
            String token = extractToken(request);
            if (token != null && jwtUtil.validateToken(token)) {
                Authentication auth = createAuthentication(token, request);
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        } catch (Exception e) {
            response.sendError(HttpStatus.UNAUTHORIZED.value(), "Invalid token");
            return;
        }
        
        filterChain.doFilter(request, response);
    }

    private Authentication createAuthentication(String token, HttpServletRequest request) {
        Claims claims = jwtUtil.extractAllClaims(token);
        
        // Get roles from either JWT claims or headers (your choice)
        List<String> roles = getRoles(claims, request);
        
        List<SimpleGrantedAuthority> authorities = roles.stream()
            .map(role -> new SimpleGrantedAuthority( role))
            .collect(Collectors.toList());
        
        return new UsernamePasswordAuthenticationToken(
            claims.getSubject(), 
            null, 
            authorities
        );
    }

    private List<String> getRoles(Claims claims, HttpServletRequest request) {
        // Option 1: Get from JWT claims
        if (claims.containsKey("roles")) {
            return claims.get("roles", List.class);
        }
        
        // Option 2: Get from gateway headers (recommended)
        String rolesHeader = request.getHeader("X-User-Roles");
        return rolesHeader != null ? 
            Arrays.asList(rolesHeader.split(",")) :

                Collections.emptyList();
    }

    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }
}