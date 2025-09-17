package co.com.crediya.model.loanapplication.gateways;

import co.com.crediya.model.loanapplication.UserSnapshot;
import reactor.core.publisher.Mono;

public interface UserRepository {
    Mono<Boolean> existsById(String userId);

    Mono<UserSnapshot> getUserSnapshot(String token);
}
