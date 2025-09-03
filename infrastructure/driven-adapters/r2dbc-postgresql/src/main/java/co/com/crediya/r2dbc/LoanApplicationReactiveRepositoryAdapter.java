package co.com.crediya.r2dbc;

import co.com.crediya.model.loanapplication.ApplicationStatus;
import co.com.crediya.model.loanapplication.LoanApplication;
import co.com.crediya.model.loanapplication.gateways.LoanApplicationRepository;
import co.com.crediya.r2dbc.entity.LoanApplicationEntity;
import co.com.crediya.r2dbc.helper.ReactiveAdapterOperations;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

import org.reactivecommons.utils.ObjectMapper;
import org.springframework.stereotype.Repository;

@Repository
public class LoanApplicationReactiveRepositoryAdapter extends ReactiveAdapterOperations<
        LoanApplication,
        LoanApplicationEntity,
        UUID,
        LoanApplicationReactiveRepository
        > implements LoanApplicationRepository {

    private final LoanApplicationReactiveRepository reactiveRepository;

    public LoanApplicationReactiveRepositoryAdapter(LoanApplicationReactiveRepository repository,
                                                    ObjectMapper mapper) {
        super(repository, mapper, d -> mapper.map(d, LoanApplication.class));
        this.reactiveRepository = repository;
    }

    @Override
    public Mono<LoanApplication> save(LoanApplication application) {
        return Mono.just(application)
                .map(this::toData)
                .flatMap(reactiveRepository::save)
                .map(this::toDomain);
    }

    @Override
    public Mono<LoanApplication> findById(UUID id) {
        return reactiveRepository.findById(id)
                .map(this::toDomain);
    }

    @Override
    public Mono<LoanApplication> updateStatus(UUID id, ApplicationStatus status) {
        return reactiveRepository.findById(id)
                .map(data -> data.toBuilder().status(status).build())
                .flatMap(reactiveRepository::save)
                .map(this::toDomain);
    }

    @Override
    public Flux<LoanApplication> findAll() {
        return reactiveRepository.findAll()
                .map(this::toDomain);
    }

    @Override
    public Flux<LoanApplication> findAllByUser(String userId) {
        return reactiveRepository.findAllByUserId(userId)
                .map(this::toDomain);
    }

    @Override
    public Flux<LoanApplication> findAllByStatus(ApplicationStatus status) {
        return reactiveRepository.findAllByStatus(status)
                .map(this::toDomain);
    }

    protected LoanApplicationEntity toData(LoanApplication domain) {
        return LoanApplicationEntity.builder()
                .id(domain.getId())
                .userId(domain.getUserId())
                .loanTypeId(domain.getLoanTypeId())
                .amount(domain.getAmount())
                .termMonths(domain.getTermMonths())
                .status(domain.getStatus())
                .createdAt(domain.getCreatedAt())
                .build();
    }

    private LoanApplication toDomain(LoanApplicationEntity entity) {
        return LoanApplication.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .loanTypeId(entity.getLoanTypeId())
                .amount(entity.getAmount())
                .termMonths(entity.getTermMonths())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}