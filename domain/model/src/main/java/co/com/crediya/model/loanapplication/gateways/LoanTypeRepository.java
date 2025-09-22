package co.com.crediya.model.loanapplication.gateways;

import java.util.UUID;

import co.com.crediya.model.loanapplication.LoanType;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface LoanTypeRepository {
    Mono<LoanType> findById(UUID loanTypeId);

    Flux<LoanType> findAll();
}