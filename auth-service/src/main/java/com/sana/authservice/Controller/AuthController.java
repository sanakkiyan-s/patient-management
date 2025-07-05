package com.sana.authservice.Controller;

import com.sana.authservice.DTO.JWTResponse;
import com.sana.authservice.DTO.JwtRefreshToken;
import com.sana.authservice.DTO.LoginRequestDTO;
import com.sana.authservice.DTO.RegReqDTO;
import com.sana.authservice.Model.MessageResponse;
import com.sana.authservice.Service.AuthService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping
@Slf4j
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }


    @PostMapping("/api/v1/auth/register")
    public ResponseEntity<?> reg(@Valid @RequestBody RegReqDTO regReqDTO) {
        authService.register(regReqDTO);
        return ResponseEntity.ok().body("User registered successfully");
    }

    @PostMapping("/api/v1/auth/login")
    public ResponseEntity<JWTResponse> login(@RequestBody LoginRequestDTO regReqDTO, HttpServletResponse httpServletResponse) {
        JWTResponse jwtResponse = authService.login(regReqDTO, httpServletResponse);
        return ResponseEntity.ok(jwtResponse);
    }

    @PostMapping("/api/auth/refresh")
    public ResponseEntity<JWTResponse> refreshToken(@CookieValue(name = "JWT_REFRESH_TOKEN") String refreshToken, HttpServletResponse httpServletResponse) {
        JWTResponse jwtResponse = authService.refreshToken(refreshToken,httpServletResponse);
        return ResponseEntity.ok(jwtResponse);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse httpServletResponse, @RequestHeader(name= HttpHeaders.AUTHORIZATION) String authorizationHeader , @CookieValue(name = "JWT_REFRESH_TOKEN") String refreshToken ) {

        String token= authorizationHeader.substring(7);
        ResponseEntity<MessageResponse> logout = authService.logout(httpServletResponse, token, refreshToken);

        return logout;
    }

    @GetMapping("/api/users/exists/{userId}")
    public ResponseEntity<Boolean> userExists(@PathVariable String userId) {

        log.info("userId is " + userId+"  checking");
        Boolean body = authService.existsById(userId);
        log.info("userId is " +  body);

        return ResponseEntity.ok(
                body
        );
    }

}
