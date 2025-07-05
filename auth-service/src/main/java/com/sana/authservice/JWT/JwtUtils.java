package com.sana.authservice.JWT;

import com.sana.authservice.Config.AppUserDetails;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@Getter
public class JwtUtils {

    @Value("${app.jwtSecret:eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiYWRtaW4iOnRydWV9.TJVA95OrM7E2cBab30RMHrHDcEfxjoYZgeFONFh7HgQ}")
    private String jwtSecret;

    @Value("${app.jwtExpirationMs:3600000}")
    private Long jwtExpirationMs;

    @Value("${jwt.refresh.expiration:604800000}")
    private long refreshExpirationMs;

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }


    public String generateToken(UserDetails user) {

        List<String> roles = user.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList());

        return getToken(user, roles, jwtExpirationMs);

    }

    public String getRefreshToken(UserDetails user) {

        List<String> roles = user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        return getToken(user, roles, refreshExpirationMs);
    }

    private String getToken(UserDetails user, List<String> roles, Long ExpirationMs) {
        return Jwts.builder()
                .subject(user.getUsername())
                .claim("roles", roles)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + ExpirationMs))
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    public Claims ExtractClaims(String token) {
        return Jwts.parser()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public String getUsernameFromToken(String token) {
        return ExtractClaims(token).getSubject();
    }

    public Boolean validateToken(String token, String username) {

        String extractedUsername = getUsernameFromToken(token);

        return (extractedUsername.equals(username) && !isTokenExpired(token));

    }

    public Boolean isTokenExpired(String token) {
        Date expiration = ExtractClaims(token).getExpiration();
        return expiration.before(new Date());
    }

    public List<String> getRolesFromToken(String token) {
        Object obj = ExtractClaims(token).get("roles");

        if (obj instanceof List<?>) {
            return ((List<?>) obj).stream()
                    .map(Object::toString)
                    .collect(Collectors.toList());
        }
        return List.of();
    }

    public Boolean validateToken(String token) {

        try {
            Jwts.parser()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);

            return true;
        } catch (
                SignatureException e) {
            log.error("Invalid JWT signature: {}", e.getMessage());
        } catch (
                MalformedJwtException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
        } catch (
                ExpiredJwtException e) {
            log.error("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.error("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("JWT claims string is empty: {}", e.getMessage());
        }
        return false;
    }


}



