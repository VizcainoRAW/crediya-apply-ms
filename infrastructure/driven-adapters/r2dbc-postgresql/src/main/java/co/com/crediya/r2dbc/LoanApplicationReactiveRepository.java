package co.com.crediya.r2dbc;

import java.util.UUID;

import org.springframework.data.repository.query.ReactiveQueryByExampleExecutor;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import co.com.crediya.model.loanapplication.ApplicationStatus;
import co.com.crediya.r2dbc.entity.LoanApplicationEntity;
import reactor.core.publisher.Flux;

public interface LoanApplicationReactiveRepository extends ReactiveCrudRepository<LoanApplicationEntity, UUID>, ReactiveQueryByExampleExecutor<LoanApplicationEntity> {

    Flux<LoanApplicationEntity> findAllByUserId(String userId);
    
    Flux<LoanApplicationEntity> findAllByStatus(ApplicationStatus status);
}