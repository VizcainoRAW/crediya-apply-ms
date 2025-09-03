package co.com.crediya.r2dbc;

import co.com.crediya.model.loanapplication.LoanType;
import co.com.crediya.model.loanapplication.gateways.LoanTypeRepository;
import co.com.crediya.r2dbc.entity.LoanTypeEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class LoanTypeRepositoryAdapter implements LoanTypeRepository {

    private final LoanTypeReactiveRepoository reactiveRepository;

    @Override
    public Mono<LoanType> findById(UUID loanTypeId) {
        return reactiveRepository.findById(loanTypeId)
                .map(this::toDomain);
    }

    private LoanType toDomain(LoanTypeEntity data) {
        return LoanType.builder()
                .id(data.getId())
                .name(data.getName())
                .build();
    }
}