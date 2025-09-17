package co.com.crediya.consumer;

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
public class RestConsumer implements UserRepository{
    private final WebClient client;


    @Override
    @CircuitBreaker(name = "testGet", fallbackMethod = "fallbackExistsById")
    public Mono<Boolean> existsById(String userId) {
        return client.get()
            .uri(uriBuilder -> uriBuilder.path("/api/users/{id}/exists").build(userId))
            .retrieve()
            .bodyToMono(UserExistsResponse.class)
            .map(UserExistsResponse::getData);
    }

    public Mono<Boolean> fallbackExistsById(String userId, Throwable t) {
        log.error("Fallback triggered for userId {} due to {}", userId, t.getMessage());
        return Mono.just(false);
    }

    @Override
    public Mono<UserSnapshot> getUserSnapshot(String token) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getUserSnapshot'");
    }
}
