package org.example.apigateway.config;


import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.List;
import java.util.stream.Collectors;

@Configuration
public class UserHeaderFilterConfig {

    @Bean
    public GlobalFilter userHeaderFilter() {
        return (exchange, chain) -> exchange.getPrincipal()
                .filter(principal -> principal instanceof JwtAuthenticationToken)
                .cast(JwtAuthenticationToken.class)
                .flatMap(auth -> {
                    String userId = auth.getToken().getSubject();

                    List<String> roles = auth.getToken().getClaimAsStringList("roles");
                    String rolesHeader = roles == null
                            ? ""
                            : roles.stream().collect(Collectors.joining(","));

                    var mutatedRequest = exchange.getRequest()
                            .mutate()
                            .headers(headers -> {
                                headers.remove("X-User-Id");
                                headers.remove("X-User-Roles");
                                headers.add("X-User-Id", userId);
                                headers.add("X-User-Roles", rolesHeader);
                            })
                            .build();

                    var mutatedExchange = exchange.mutate()
                            .request(mutatedRequest)
                            .build();

                    return chain.filter(mutatedExchange);
                })
                .switchIfEmpty(chain.filter(exchange));
    }
}