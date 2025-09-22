package co.com.crediya.r2dbc;

import java.util.UUID;

import org.springframework.data.domain.Sort;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.query.ReactiveQueryByExampleExecutor;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import co.com.crediya.model.loanapplication.ApplicationStatus;
import co.com.crediya.r2dbc.entity.LoanApplicationEntity;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface LoanApplicationReactiveRepository extends ReactiveCrudRepository<LoanApplicationEntity, UUID>, ReactiveQueryByExampleExecutor<LoanApplicationEntity> {

    Flux<LoanApplicationEntity> findAllByUserId(String userId);

    Flux<LoanApplicationEntity> findAllByStatus(ApplicationStatus status);

    @Query("SELECT * FROM loan_applications ORDER BY :#{#sort.toString()} LIMIT :limit OFFSET :offset")
    Flux<LoanApplicationEntity> findAllWithPagination(@Param("offset") int offset,
                                                      @Param("limit") int limit,
                                                      @Param("sort") Sort sort);

    @Query("SELECT * FROM loan_applications WHERE user_id = :userId ORDER BY :#{#sort.toString()} LIMIT :limit OFFSET :offset")
    Flux<LoanApplicationEntity> findByUserIdWithPagination(@Param("userId") String userId,
                                                           @Param("offset") int offset,
                                                           @Param("limit") int limit,
                                                           @Param("sort") Sort sort);


    @Query("SELECT COUNT(*) FROM loan_applications WHERE user_id = :userId")
    Mono<Long> countByUserId(@Param("userId") String userId);


    @Query("SELECT * FROM loan_applications WHERE status = :status ORDER BY :#{#sort.toString()} LIMIT :limit OFFSET :offset")
    Flux<LoanApplicationEntity> findByStatusWithPagination(@Param("status") ApplicationStatus status,
                                                           @Param("offset") int offset,
                                                           @Param("limit") int limit,
                                                           @Param("sort") Sort sort);


    @Query("SELECT COUNT(*) FROM loan_applications WHERE status = :status")
    Mono<Long> countByStatus(@Param("status") ApplicationStatus status);
}