package com.sana.authservice.Service;

import com.sana.authservice.Config.AppUserDetails;
import com.sana.authservice.Config.AppUserDetailsService;
import com.sana.authservice.DTO.JWTResponse;
import com.sana.authservice.DTO.JwtRefreshToken;
import com.sana.authservice.DTO.LoginRequestDTO;
import com.sana.authservice.DTO.RegReqDTO;
import com.sana.authservice.JWT.JwtUtils;
import com.sana.authservice.Model.AppUser;
import com.sana.authservice.Model.MessageResponse;
import com.sana.authservice.Model.Roles;
import com.sana.authservice.Redis.RefreshTokenService;
import com.sana.authservice.Repo.UserRepo;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityManager;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Slf4j
public class AuthService {

    private UserRepo userRepo;
    private JwtUtils jwtUtils;
    private PasswordEncoder passwordEncoder;
    private AuthenticationManager authenticationManager;
    private RefreshTokenService refreshTokenService;
    private AppUserDetailsService appUserDetailsService;

    public void register(RegReqDTO regReqDTO) {

        if(userRepo.existsByEmail(regReqDTO.getEmail())) {
            throw new RuntimeException("User already exists with this email");
        }

        AppUser user=AppUser.builder()
                .email(regReqDTO.getEmail())
                .password(passwordEncoder.encode(regReqDTO.getPassword())).build();

        Set<Roles> roles = new HashSet<>();
        if(regReqDTO.getRoles() == null || regReqDTO.getRoles().isEmpty()) {
            roles.add(Roles.ROLE_USER);
        } else {

            regReqDTO.getRoles().forEach(role -> {
                switch (role) {
                    case "admin" -> roles.add(Roles.ROLE_ADMIN);
                    case "user" -> roles.add(Roles.ROLE_USER);
                    default -> throw new RuntimeException("Role not found: " + role);
                }
            });

        }

        user.setRole(roles);
        userRepo.save(user);





    }

    public JWTResponse login(LoginRequestDTO loginRequestDTO, HttpServletResponse response) {
        log.info("User {} is trying to log in", loginRequestDTO.getEmail());

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequestDTO.getEmail(),
                        loginRequestDTO.getPassword()));

        log.info("User {} logged in successfully", loginRequestDTO.getEmail());

        SecurityContextHolder.getContext().setAuthentication(authentication);

        AppUserDetails user = (AppUserDetails) authentication.getPrincipal();
        String jwtAccessToken = jwtUtils.generateToken(user);
        String jwtRefreshToken = jwtUtils.getRefreshToken(user);

        Date expirationDate = jwtUtils.ExtractClaims(jwtRefreshToken).getExpiration();
        Instant expiry = expirationDate.toInstant();

        refreshTokenService.saveRefreshToken(
                jwtRefreshToken,
                loginRequestDTO.getEmail(),
                Instant.now().plus(jwtUtils.getRefreshExpirationMs(), ChronoUnit.MILLIS));

        ResponseCookie cookie = ResponseCookie.from("JWT_REFRESH_TOKEN", jwtRefreshToken)
                .httpOnly(true)
                .secure(true)
                .path("/api/auth/refresh")
                .maxAge(jwtUtils.getRefreshExpirationMs() / 1000)
                .sameSite("Strict")
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());



        log.info("Generated JWT token for user {}: {}", loginRequestDTO.getEmail(), jwtAccessToken);

        AppUserDetails userDetails=(AppUserDetails) authentication.getPrincipal();

        log.info("User {} logged in successfully", loginRequestDTO.getEmail());

        List<String> role = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        log.info("Roles for user {}: {}", loginRequestDTO.getEmail(), role);

        return new JWTResponse(jwtAccessToken,
                userDetails.getUsername(),role
                );
    }

    public JWTResponse refreshToken(String refreshToken,HttpServletResponse  response) {

        log.info("User {} is trying to refresh token", refreshToken);

        if(jwtUtils.validateToken(refreshToken)) {

            log.info("User {} is validate refresh token", refreshToken);
            if (refreshTokenService.isTokenValid(refreshToken)) {

                log.info("User {} is validate  refresh token in redis", refreshToken);

                String name= jwtUtils.getUsernameFromToken(refreshToken);
                UserDetails userDetails = appUserDetailsService.loadUserByUsername(name);
                String newAccessToken = jwtUtils.generateToken(userDetails);
                String NewRefreshToken =jwtUtils.getRefreshToken(userDetails);

                refreshTokenService.deleteByToken(refreshToken);
                refreshTokenService.saveRefreshToken(NewRefreshToken, userDetails.getUsername(), Instant.now().plus(jwtUtils.getRefreshExpirationMs(), ChronoUnit.MILLIS));

                ResponseCookie cookie = ResponseCookie.from("JWT_REFRESH_TOKEN", NewRefreshToken)
                        .httpOnly(true)
                        .secure(true)
                        .path("/api/auth/refresh")
                        .maxAge(jwtUtils.getRefreshExpirationMs() / 1000)
                        .sameSite("Strict")
                        .build();

                response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

                return new JWTResponse(newAccessToken, userDetails.getUsername(), userDetails.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList()));

            }else  {
                log.info("User {} is  refresh token is not in redis", refreshToken);
                throw new RuntimeException("Invalid refresh token in redis");
            }
        }else {
            throw new RuntimeException("Invalid structure refresh token ");
        }



    }

    public ResponseEntity<MessageResponse> logout(HttpServletResponse httpServletResponse, String token, String refreshToken) {
        log.info("User going to logged out");
        if (jwtUtils.validateToken(token)) {
            log.info("User token is valid");
            refreshTokenService.blacklistAccessToken(token,jwtUtils.ExtractClaims(token).getExpiration());
            log.info("User token is black listed");


        }
        refreshTokenService.deleteByToken(refreshToken);
            log.info("User refresh token is deleted");

        ResponseCookie cookie= ResponseCookie.from("JWT_REFRESH_TOKEN","")
                .httpOnly(true)
                .secure(true)
                .maxAge(0)
                .sameSite("Strict")
                .build();

        httpServletResponse.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        return ResponseEntity.ok(new MessageResponse("Logged out successfully"));
    }

    public Boolean existsById(String userId) {
        return userRepo.existsByEmail(userId);
    }
}
