package com.sana.apigateway.Jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.List;

@Service
public class JwtUtil {


    @Value("${app.jwtSecret:eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiYWRtaW4iOnRydWV9.TJVA95OrM7E2cBab30RMHrHDcEfxjoYZgeFONFh7HgQ}")
    private String jwtSecret;

    @Value("${app.jwtExpirationMs:3600000}")
    private Long jwtExpirationMs;

    @Value("${jwt.refresh.expiration:604800000}")
    private long refreshExpirationMs;


    private Key getSigningKey() {

        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }


    public Claims extractClaims(String token) {
        return Jwts.parser()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public List<String> extractRoles(String token) {
        return extractClaims(token).get("roles", List.class);
    }


    public Boolean validateToken(String token) {
        try {
            extractClaims(token);
            return true;
        }catch (Exception e) {
            return false;
        }
    }




}
