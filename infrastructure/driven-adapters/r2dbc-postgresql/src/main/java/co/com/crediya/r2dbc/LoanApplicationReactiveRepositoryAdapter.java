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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Repository
public class LoanApplicationReactiveRepositoryAdapter extends ReactiveAdapterOperations<
        LoanApplication,
        LoanApplicationEntity,
        UUID,
        LoanApplicationReactiveRepository
        > implements LoanApplicationRepository {

    private static final Logger logger = LoggerFactory.getLogger(LoanApplicationReactiveRepositoryAdapter.class);
    private final LoanApplicationReactiveRepository reactiveRepository;

    public LoanApplicationReactiveRepositoryAdapter(LoanApplicationReactiveRepository repository,
                                                    ObjectMapper mapper) {
        super(repository, mapper, d -> mapper.map(d, LoanApplication.class));
        this.reactiveRepository = repository;
        logger.info("LoanApplicationReactiveRepositoryAdapter initialized");
    }

    @Override
    public Mono<LoanApplication> save(LoanApplication application) {
        logger.info("Attempting to save loan application for user: {}", application.getUserId());
        logger.debug("Loan application details - Amount: {}, Term: {} months, LoanType: {}", 
                    application.getAmount(), application.getTermMonths(), application.getLoanTypeId());
        
        return Mono.just(application)
                .doOnNext(app -> logger.trace("Converting domain object to entity for application: {}", app.getId()))
                .map(this::toData)
                .doOnNext(entity -> logger.trace("Entity created, setting as new"))
                .map(LoanApplicationEntity::setAsNew)
                .doOnNext(entity -> logger.debug("Persisting loan application entity with ID: {}", entity.getId()))
                .flatMap(entity -> reactiveRepository.save(entity)
                        .doOnSuccess(savedEntity -> logger.info("Successfully saved loan application with ID: {} for user: {}", 
                                                               savedEntity.getId(), savedEntity.getUserId()))
                        .doOnError(error -> logger.error("Failed to save loan application for user: {}. Error: {}", 
                                                        application.getUserId(), error.getMessage(), error)))
                .map(this::toDomain)
                .doOnNext(saved -> logger.debug("Converted saved entity back to domain object"))
                .doOnError(error -> logger.error("Error in save operation for user: {}. Error: {}", 
                                                application.getUserId(), error.getMessage(), error));
    }

    @Override
    public Mono<LoanApplication> findById(UUID id) {
        logger.info("Finding loan application by ID: {}", id);
        
        return reactiveRepository.findById(id)
                .doOnNext(entity -> logger.debug("Found loan application entity with ID: {} for user: {}", 
                                                entity.getId(), entity.getUserId()))
                .map(this::toDomain)
                .doOnNext(domain -> logger.debug("Successfully converted entity to domain object for ID: {}", id))
                .doOnSuccess(result -> {
                    if (result != null) {
                        logger.info("Successfully retrieved loan application with ID: {}", id);
                    } else {
                        logger.warn("No loan application found with ID: {}", id);
                    }
                })
                .doOnError(error -> logger.error("Error finding loan application by ID: {}. Error: {}", 
                                                id, error.getMessage(), error));
    }

    @Override
    public Mono<LoanApplication> updateStatus(UUID id, ApplicationStatus status) {
        logger.info("Updating status for loan application ID: {} to status: {}", id, status);
        
        return reactiveRepository.findById(id)
                .doOnNext(entity -> logger.debug("Found existing loan application for status update: {}", id))
                .switchIfEmpty(Mono.defer(() -> {
                    logger.warn("Loan application with ID: {} not found for status update", id);
                    return Mono.empty();
                }))
                .map(data -> {
                    logger.debug("Converting entity to builder and updating status from {} to {}", 
                               data.getStatus(), status);
                    return data.toBuilder().status(status).build();
                })
                .flatMap(updatedEntity -> reactiveRepository.save(updatedEntity)
                        .doOnSuccess(saved -> logger.info("Successfully updated loan application ID: {} status to: {}", 
                                                         id, status))
                        .doOnError(error -> logger.error("Failed to save status update for loan application ID: {}. Error: {}", 
                                                        id, error.getMessage(), error)))
                .map(this::toDomain)
                .doOnError(error -> logger.error("Error updating status for loan application ID: {} to status: {}. Error: {}", 
                                                id, status, error.getMessage(), error));
    }

    @Override
    public Flux<LoanApplication> findAll() {
        logger.info("Retrieving all loan applications");
        
        return reactiveRepository.findAll()
                .doOnSubscribe(subscription -> logger.debug("Starting to fetch all loan applications"))
                .map(this::toDomain)
                .doOnNext(app -> logger.trace("Converted loan application entity to domain: {}", app.getId()))
                .doOnComplete(() -> logger.info("Successfully retrieved all loan applications"))
                .doOnError(error -> logger.error("Error retrieving all loan applications. Error: {}", 
                                                error.getMessage(), error))
                .collectList()
                .doOnNext(list -> logger.info("Total loan applications retrieved: {}", list.size()))
                .flatMapMany(Flux::fromIterable);
    }

    @Override
    public Flux<LoanApplication> findAllByUserId(String userId) {
        logger.info("Finding all loan applications for user: {}", userId);
        
        return reactiveRepository.findAllByUserId(userId)
                .doOnSubscribe(subscription -> logger.debug("Starting to fetch loan applications for user: {}", userId))
                .map(this::toDomain)
                .doOnNext(app -> logger.trace("Retrieved loan application ID: {} for user: {}", app.getId(), userId))
                .doOnComplete(() -> logger.info("Successfully retrieved all loan applications for user: {}", userId))
                .doOnError(error -> logger.error("Error retrieving loan applications for user: {}. Error: {}", 
                                                userId, error.getMessage(), error))
                .collectList()
                .doOnNext(list -> logger.info("Total loan applications found for user {}: {}", userId, list.size()))
                .flatMapMany(Flux::fromIterable);
    }

    @Override
    public Flux<LoanApplication> findAllByStatus(ApplicationStatus status) {
        logger.info("Finding all loan applications with status: {}", status);
        
        return reactiveRepository.findAllByStatus(status)
                .doOnSubscribe(subscription -> logger.debug("Starting to fetch loan applications with status: {}", status))
                .map(this::toDomain)
                .doOnNext(app -> logger.trace("Retrieved loan application ID: {} with status: {}", app.getId(), status))
                .doOnComplete(() -> logger.info("Successfully retrieved all loan applications with status: {}", status))
                .doOnError(error -> logger.error("Error retrieving loan applications with status: {}. Error: {}", 
                                                status, error.getMessage(), error))
                .collectList()
                .doOnNext(list -> logger.info("Total loan applications found with status {}: {}", status, list.size()))
                .flatMapMany(Flux::fromIterable);
    }

    protected LoanApplicationEntity toData(LoanApplication domain) {
        logger.trace("Converting domain object to entity for loan application: {}", domain.getId());
        
        try {
            LoanApplicationEntity entity = LoanApplicationEntity.builder()
                    .id(domain.getId())
                    .userId(domain.getUserId())
                    .loanTypeId(domain.getLoanTypeId())
                    .amount(domain.getAmount())
                    .termMonths(domain.getTermMonths())
                    .status(domain.getStatus())
                    .createdAt(domain.getCreatedAt())
                    .build();
            
            logger.trace("Successfully converted domain to entity for loan application: {}", domain.getId());
            return entity;
        } catch (Exception e) {
            logger.error("Error converting domain to entity for loan application: {}. Error: {}", 
                        domain.getId(), e.getMessage(), e);
            throw e;
        }
    }

    private LoanApplication toDomain(LoanApplicationEntity entity) {
        logger.trace("Converting entity to domain object for loan application: {}", entity.getId());
        
        try {
            LoanApplication domain = LoanApplication.builder()
                    .id(entity.getId())
                    .userId(entity.getUserId())
                    .loanTypeId(entity.getLoanTypeId())
                    .amount(entity.getAmount())
                    .termMonths(entity.getTermMonths())
                    .status(entity.getStatus())
                    .createdAt(entity.getCreatedAt())
                    .build();
            
            logger.trace("Successfully converted entity to domain for loan application: {}", entity.getId());
            return domain;
        } catch (Exception e) {
            logger.error("Error converting entity to domain for loan application: {}. Error: {}", 
                        entity.getId(), e.getMessage(), e);
            throw e;
        }
    }
}