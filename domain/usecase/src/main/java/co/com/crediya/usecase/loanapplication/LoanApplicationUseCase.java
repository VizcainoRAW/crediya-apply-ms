package co.com.crediya.usecase.loanapplication;

import co.com.crediya.model.loanapplication.ApplicationStatus;
import co.com.crediya.model.loanapplication.LoanApplication;
import co.com.crediya.model.loanapplication.LoanType;
import co.com.crediya.model.loanapplication.UserSnapshot;
import co.com.crediya.model.loanapplication.gateways.LoanApplicationRepository;
import co.com.crediya.model.loanapplication.gateways.LoanTypeRepository;
import co.com.crediya.model.loanapplication.valuobject.PageRequest;
import co.com.crediya.model.loanapplication.valuobject.PageResponse;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@RequiredArgsConstructor
public class LoanApplicationUseCase {

    private final LoanApplicationRepository loanApplicationRepository;
    private final LoanTypeRepository loanTypeRepository;

    public Mono<LoanApplication> createLoanApplication(LoanApplication application, UserSnapshot userSnapshot) {
        return validateLoanType(application.getLoanTypeId())
                .then(validateLoanAmount(application.getAmount()))
                .then(validateLoanTerm(application.getTermMonths()))
                .then(createApplicationWithUserContext(application, userSnapshot))
                .flatMap(loanApplicationRepository::save);
    }

    public Flux<LoanApplication> findAllLoansByUser(UserSnapshot userSnapshot) {
        return loanApplicationRepository.findAllByUserId(userSnapshot.id());
    }

    public Flux<LoanApplication> findAllPendingLoans(UserSnapshot userSnapshot) {
        if (!userSnapshot.canReviewApplications()) {
            return Flux.error(new SecurityException(
                    "Insufficient permissions. Role " + userSnapshot.role() + " cannot review applications"));
        }

        return loanApplicationRepository.findAllByStatus(ApplicationStatus.PENDING_REVIEW);
    }

    private Mono<Void> validateLoanType(UUID loanTypeId) {
        return loanTypeRepository.findById(loanTypeId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException(
                    "El tipo de préstamo especificado no existe")))
                .then();
    }

    public Flux<LoanType> findAllLoanTypes(){
        return loanTypeRepository.findAll();
    }

    public Mono<PageResponse<LoanApplication>> execute(PageRequest pageRequest) {
        return validatePageRequest(pageRequest)
                .then(loanApplicationRepository.findAll(pageRequest));
    }

    private Mono<Void> validatePageRequest(PageRequest pageRequest) {
        if (pageRequest == null) {
            return Mono.error(new IllegalArgumentException("PageRequest cannot be null"));
        }

        if (pageRequest.page() < 0) {
            return Mono.error(new IllegalArgumentException("Page number cannot be negative"));
        }

        if (pageRequest.size() <= 0 || pageRequest.size() > 100) {
            return Mono.error(new IllegalArgumentException("Page size must be between 1 and 100"));
        }

        return Mono.empty();
    }

    private Mono<Void> validateLoanAmount(BigDecimal amount) {
        if (amount == null) {
            return Mono.error(new IllegalArgumentException("Amount cannot be null"));
        }
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            return Mono.error(new IllegalArgumentException("Amount must be greater than zero"));
        }
        if (amount.compareTo(new BigDecimal("100000000")) > 0) {
            return Mono.error(new IllegalArgumentException("Amount exceeds maximum limit of 100,000,000"));
        }

        return Mono.empty();
    }

    private Mono<Void> validateLoanTerm(Integer termMonths) {
        if (termMonths == null) {
            return Mono.error(new IllegalArgumentException("Term months cannot be null"));
        }
        if (termMonths <= 0 || termMonths > 360) {
            return Mono.error(new IllegalArgumentException(
                    "Term months must be between 1 and 360, provided: " + termMonths));
        }

        return Mono.empty();
    }

    private Mono<LoanApplication> createApplicationWithUserContext(LoanApplication application, UserSnapshot userSnapshot) {
        UUID applicationId = UUID.randomUUID();

        return Mono.just(LoanApplication.builder()
                        .id(applicationId)
                        .userId(userSnapshot.id())
                        .amount(application.getAmount())
                        .termMonths(application.getTermMonths())
                        .loanTypeId(application.getLoanTypeId())
                        .status(ApplicationStatus.PENDING_REVIEW)
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build());
    }
}