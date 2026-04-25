package org.example.apigateway.config;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class TokenVersionService {

    private final ReactiveStringRedisTemplate redis;
    private final WebClient authWebClient;

    public Mono<Long> resolveCurrentVersion(String sub) {
        String key = "usr:ver:" + sub;

        return redis.opsForValue()
                .get(key)
                .flatMap(value -> {
                    try {
                        return Mono.just(Long.parseLong(value));
                    } catch (NumberFormatException e) {
                        return Mono.empty();
                    }
                })
                .switchIfEmpty(fetchFromAuthAndCache(sub, key));
    }

    private Mono<Long> fetchFromAuthAndCache(String sub, String key) {
        return authWebClient.get()
                .uri("/internal/auth/token-version/{userId}", sub)
                .retrieve()
                .bodyToMono(Long.class)
                .flatMap(version ->
                        redis.opsForValue()
                                .set(key, String.valueOf(version), Duration.ofDays(1))
                                .thenReturn(version)
                );
    }
}