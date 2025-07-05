package com.sana.apigateway.Filter;

import com.sana.apigateway.Jwt.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
@Slf4j
public class JwtFilterGatewayFilterFactory extends AbstractGatewayFilterFactory<Object> {

    private final JwtUtil jwtUtils;
    private final WebClient webClient;

    public JwtFilterGatewayFilterFactory(JwtUtil jwtUtils, WebClient.Builder webClientBuilder, @Value("${auth.service.url:http://auth-service:4009}") String authServiceUrl) {
        this.jwtUtils = jwtUtils;
        this.webClient = webClientBuilder.baseUrl(authServiceUrl).build();
    }

    @Override
    public GatewayFilter apply(Object config) {

        return ((exchange, chain) -> {

            log.info("entered the filter");



            String token = exchange.getRequest().getHeaders().getFirst("Authorization");

            log.info("token is " + token);

            if (token == null || !token.startsWith("Bearer ")) {
                log.info("token is null");
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }

            String authToken = token.substring(7);

            log.info("authToken is " + authToken);

            if (!jwtUtils.validateToken(authToken)) {

                log.info("authToken is not valid structure");

                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }


            String userId = jwtUtils.extractClaims(authToken).getSubject();

            log.info("userId is " + userId);

            return webClient.get()

                    .uri("/api/users/exists/{userId}", userId)
                    .header(HttpHeaders.AUTHORIZATION, token)

                    .retrieve().bodyToMono(Boolean.class)
                    .flatMap(exits -> {
                        if (!exits) {

                            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                            return exchange.getResponse().setComplete();
                        }

                        ServerHttpRequest mutateRequest = exchange.getRequest().mutate()
                                .header("X-User-Id", userId)
                                .header("X-User-Roles", String.join(",", jwtUtils.extractRoles(authToken)))
                                .header(HttpHeaders.AUTHORIZATION, token)
                                .build();

                        return chain.filter(exchange.mutate().request(mutateRequest).build());


                    }).onErrorResume(e -> {
                        log.error("Error occurred during user existence check: ", e);

                        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                        return exchange.getResponse().setComplete();
                    });


        });
    }
}
