package org.example.apigateway.config;

import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

public class JwtVersionFilter implements WebFilter {

    private final TokenVersionService tokenVersionService;

    public JwtVersionFilter(TokenVersionService tokenVersionService) {
        this.tokenVersionService = tokenVersionService;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        return exchange.getPrincipal()
                .filter(principal -> principal instanceof JwtAuthenticationToken)
                .cast(JwtAuthenticationToken.class)
                .flatMap(authentication -> {
                    Jwt jwt = authentication.getToken();

                    String sub = jwt.getSubject();
                    Number verNum = jwt.getClaim("ver");

                    if (sub == null || verNum == null) {
                        return Mono.error(new ResponseStatusException(
                                HttpStatus.UNAUTHORIZED,
                                "Unauthorized"
                        ));
                    }

                    long tokenVer = verNum.longValue();

                    return tokenVersionService.resolveCurrentVersion(sub)
                            .flatMap(currentVersion -> {
                                if (currentVersion != tokenVer) {
                                    return Mono.error(new ResponseStatusException(
                                            HttpStatus.UNAUTHORIZED,
                                            "Unauthorized"
                                    ));
                                }
                                return chain.filter(exchange);
                            });
                })
                .switchIfEmpty(chain.filter(exchange));
    }
}