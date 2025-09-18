package co.com.crediya.consumer;

import co.com.crediya.consumer.dto.TokenValidationRequest;
import co.com.crediya.consumer.dto.TokenValidationResponse;
import co.com.crediya.consumer.dto.UserDetailData;
import co.com.crediya.consumer.dto.UserExistsResponse;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import co.com.crediya.model.loanapplication.UserSnapshot;
import co.com.crediya.model.loanapplication.gateways.UserRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class RestConsumer implements UserRepository {
    private final WebClient client;
    private final JwtService localJwtService;

    @Override
    public Mono<Boolean> existsById(String userId) {
        return client.get()
            .uri(uriBuilder -> uriBuilder.path("/api/users/{id}/exists").build(userId))
            .retrieve()
            .bodyToMono(UserExistsResponse.class)
            .map(UserExistsResponse::getData);
    }

    @Override
    @CircuitBreaker(name = "auth-service", fallbackMethod = "fallbackValidateToken")
    public Mono<UserSnapshot> validateTokenAndGetUser(String token) {
        log.debug("Validating token with Auth MS");

        TokenValidationRequest request = new TokenValidationRequest(token, "loan-applications-ms");

        return client.post()
                .uri("/api/auth/validate-token")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(TokenValidationResponse.class)
                .filter(TokenValidationResponse::success)
                .map(TokenValidationResponse::data)
                .filter(data -> data.valid())
                .map(data -> mapToUserSnapshot(data.user(), data.expiresIn()))
                .doOnNext(snapshot -> log.debug("Token validated for user: {} - Role: {}",
                        snapshot.id(), snapshot.role()))
                .switchIfEmpty(Mono.error(new SecurityException("Token validation failed")));
    }

    private UserSnapshot mapToUserSnapshot(UserDetailData userData, Long expiresIn) {
        return new UserSnapshot(
                userData.id(),
                userData.role(),
                expiresIn
        );
    }

    public Mono<UserSnapshot> fallbackValidateToken(String token, Throwable t) {
        log.warn("Auth service unavailable, using local JWT validation as fallback: {}", t.getMessage());

        return Mono.fromCallable(() -> {
            Claims claims = localJwtService.validateAndExtractClaims(token);
            String userId = localJwtService.extractUserId(claims);
            String role = localJwtService.extractRole(claims);
            long remainingTime = localJwtService.getRemainingTimeMs(claims);

            log.debug("Token validated locally (fallback) for user: {} with role: {}", userId, role);

            return new UserSnapshot(userId, role, remainingTime);
        });
    }
}

