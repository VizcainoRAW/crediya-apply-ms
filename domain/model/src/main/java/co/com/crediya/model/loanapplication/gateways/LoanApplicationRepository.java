package co.com.crediya.model.loanapplication.gateways;

import java.util.UUID;

import co.com.crediya.model.loanapplication.ApplicationStatus;
import co.com.crediya.model.loanapplication.LoanApplication;
import co.com.crediya.model.loanapplication.valuobject.PageRequest;
import co.com.crediya.model.loanapplication.valuobject.PageResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface LoanApplicationRepository {

    Mono<LoanApplication> save(LoanApplication application);

    Mono<LoanApplication> findById(UUID id);

    Mono<LoanApplication> updateStatus(UUID id, ApplicationStatus status);

    Mono<PageResponse<LoanApplication>> findAll(PageRequest pageRequest);

    Flux<LoanApplication> findAllByUserId(String userId);

    Flux<LoanApplication> findAllByStatus(ApplicationStatus status);
}
