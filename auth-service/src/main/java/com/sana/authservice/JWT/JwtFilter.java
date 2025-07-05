package com.sana.authservice.JWT;

import com.sana.authservice.Config.AppUserDetailsService;
import com.sana.authservice.Redis.RefreshTokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
@AllArgsConstructor
@Component
public class JwtFilter extends OncePerRequestFilter {

    private JwtUtils jwtUtils;
    public AppUserDetailsService appUserDetailsService;
    private RefreshTokenService refreshTokenService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String jwt=parseJwt(request);


        if(jwt!=null && !jwtUtils.isTokenExpired(jwt)){
            String email = jwtUtils.getUsernameFromToken(jwt);
            if(email != null && SecurityContextHolder.getContext().getAuthentication() == null && !refreshTokenService.isAccessTokenBlacklisted(jwt)) {
                var userDetails = appUserDetailsService.loadUserByUsername(email);
                if (jwtUtils.validateToken(jwt, userDetails.getUsername())) {

                   UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );
                   authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    SecurityContextHolder.getContext().setAuthentication(authentication);




                    filterChain.doFilter(request, response);
                    return;
                } else {
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid JWT token");
                    return;
                }
            }
        } else {
           filterChain.doFilter(request, response);
        }


    }

    private String parseJwt(HttpServletRequest request) {

        String AuthHeader = request.getHeader("Authorization");
        if(AuthHeader != null && AuthHeader.startsWith("Bearer ")) {
            return AuthHeader.substring(7);
        }

        return null;
    }
}
