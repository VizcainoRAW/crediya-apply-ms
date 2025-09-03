package co.com.crediya.model.loanapplication.gateways;

import reactor.core.publisher.Mono;

public interface UserRepository {
    Mono<Boolean> existsById(String userId);
}
