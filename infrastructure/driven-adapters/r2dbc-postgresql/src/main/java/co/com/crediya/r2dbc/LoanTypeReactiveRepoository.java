package co.com.crediya.r2dbc;

import java.util.UUID;

import org.springframework.data.repository.query.ReactiveQueryByExampleExecutor;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import co.com.crediya.r2dbc.entity.LoanTypeEntity;

public interface LoanTypeReactiveRepoository extends ReactiveCrudRepository<LoanTypeEntity, UUID>, ReactiveQueryByExampleExecutor<LoanTypeEntity> {

}