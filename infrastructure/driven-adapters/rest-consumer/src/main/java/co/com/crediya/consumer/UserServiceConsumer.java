package co.com.crediya.consumer;

import co.com.crediya.consumer.config.RestConsumerProperties;
import co.com.crediya.model.loanapplication.gateways.UserRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceConsumer implements UserRepository {
    
    private final WebClient client;
    private final RestConsumerProperties restConsumerProperties;

    @Override
    @CircuitBreaker(name = "userExistsById", fallbackMethod = "userExistsByIdFallback")
    public Mono<Boolean> existsById(String userId) {
        log.debug("Checking if user exists with ID: {}", userId);
        
        return client
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path(restConsumerProperties.getEndpoints().getUserExists())
                        .build(userId))
                .retrieve()
                .bodyToMono(UserExistsResponse.class)
                .map(UserExistsResponse::getData)
                .doOnSuccess(exists -> log.debug("User {} exists: {}", userId, exists))
                .doOnError(error -> log.error("Error checking if user {} exists: {}", userId, error.getMessage()));
    }

    public Mono<Boolean> userExistsByIdFallback(String userId, Exception ex) {
        log.warn("Circuit breaker activated for user existence check. UserID: {}, Error: {}", 
                userId, ex.getMessage());

        return Mono.just(false);
    }

    @CircuitBreaker(name = "userExistsByIdQuery", fallbackMethod = "userExistsByIdFallback")
    public Mono<Boolean> existsByIdAsQuery(String userId) {
        log.debug("Checking if user exists with ID as query: {}", userId);
        
        String endpoint = restConsumerProperties.getEndpoints().getUserExistsByQuery();
        log.debug("Using endpoint: {}", endpoint);
        
        return client
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path(endpoint)
                        .queryParam("id", userId)
                        .build())
                .retrieve()
                .bodyToMono(UserExistsResponse.class)
                .flatMap(response -> {
                    log.debug("Received response: success={}, message={}, data={}", 
                            response.getSuccess(), response.getMessage(), response.getData());
                    
                    if (Boolean.TRUE.equals(response.getSuccess())) {
                        return Mono.just(Boolean.TRUE.equals(response.getData()));
                    } else {
                        log.warn("User service returned unsuccessful response: {}", response.getMessage());
                        return Mono.just(false);
                    }
                })
                .doOnSuccess(exists -> log.debug("User {} exists: {}", userId, exists))
                .doOnError(error -> log.error("Error checking if user {} exists: {}", userId, error.getMessage()))
                .onErrorReturn(false);
    }
}