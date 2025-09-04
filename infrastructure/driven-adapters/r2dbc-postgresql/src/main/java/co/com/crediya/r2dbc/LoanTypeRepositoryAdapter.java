package co.com.crediya.r2dbc;

import co.com.crediya.model.loanapplication.LoanType;
import co.com.crediya.model.loanapplication.gateways.LoanTypeRepository;
import co.com.crediya.r2dbc.entity.LoanTypeEntity;
import lombok.RequiredArgsConstructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class LoanTypeRepositoryAdapter implements LoanTypeRepository {

    private final LoanTypeReactiveRepoository reactiveRepository;
    private static final Logger logger = LoggerFactory.getLogger(LoanTypeRepositoryAdapter.class);

    @Override
    public Mono<LoanType> findById(UUID loanTypeId) {
        logger.info("Finding loan type by ID: {}", loanTypeId);
        
        return reactiveRepository.findById(loanTypeId)
                .doOnSubscribe(subscription -> logger.debug("Starting search for loan type ID: {}", loanTypeId))
                .doOnNext(entity -> {
                    logger.debug("Found loan type entity with ID: {} and name: '{}'", 
                               entity.getId(), entity.getName());
                    logger.trace("Loan type entity details - ID: {}, Name: '{}'", 
                               entity.getId(), entity.getName());
                })
                .map(entity -> {
                    logger.trace("Converting loan type entity to domain object for ID: {}", loanTypeId);
                    return this.toDomain(entity);
                })
                .doOnNext(domain -> logger.debug("Successfully converted loan type entity to domain object for ID: {}", 
                                                loanTypeId))
                .doOnSuccess(result -> {
                    if (result != null) {
                        logger.info("Successfully retrieved loan type: '{}' with ID: {}", 
                                  result.getName(), result.getId());
                    } else {
                        logger.warn("No loan type found with ID: {}", loanTypeId);
                    }
                })
                .doOnError(error -> logger.error("Error finding loan type by ID: {}. Error: {}", 
                                                loanTypeId, error.getMessage(), error))
                .doOnCancel(() -> logger.debug("Operation cancelled for finding loan type ID: {}", loanTypeId));
    }

    private LoanType toDomain(LoanTypeEntity data) {
        logger.trace("Converting loan type entity to domain - ID: {}, Name: '{}'", 
                    data.getId(), data.getName());
        
        try {
            LoanType domain = LoanType.builder()
                    .id(data.getId())
                    .name(data.getName())
                    .build();
            
            logger.trace("Successfully converted loan type entity to domain object - ID: {}", data.getId());
            return domain;
        } catch (Exception e) {
            logger.error("Error converting loan type entity to domain for ID: {}. Error: {}", 
                        data.getId(), e.getMessage(), e);
            throw e;
        }
    }
}