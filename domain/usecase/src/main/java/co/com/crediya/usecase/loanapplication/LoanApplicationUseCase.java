package co.com.crediya.usecase.loanapplication;

import co.com.crediya.model.loanapplication.ApplicationStatus;
import co.com.crediya.model.loanapplication.LoanApplication;
import co.com.crediya.model.loanapplication.UserSnapshot;
import co.com.crediya.model.loanapplication.gateways.LoanApplicationRepository;
import co.com.crediya.model.loanapplication.gateways.LoanTypeRepository;
import co.com.crediya.model.loanapplication.gateways.UserRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@RequiredArgsConstructor
public class LoanApplicationUseCase {

    private final LoanApplicationRepository loanApplicationRepository;
    private final UserRepository userRepository;
    private final LoanTypeRepository loanTypeRepository;

    public Mono<LoanApplication> createLoanApplication(LoanApplication application, UserSnapshot userSnapshot) {
        return validateLoanType(application.getLoanTypeId())
                .then(validateLoanAmount(application.getAmount()))
                .then(validateLoanTerm(application.getTermMonths()))
                .then(createApplicationWithUserContext(application, userSnapshot))
                .flatMap(loanApplicationRepository::save);
    }

    public Flux<LoanApplication> findAccessibleApplications(UserSnapshot userSnapshot) {
        if (userSnapshot.canReviewApplications()) {
            return loanApplicationRepository.findAll();
        } else if (userSnapshot.isClient()) {
            return loanApplicationRepository.findAllByUserId(userSnapshot.id());
        } else {
            return Flux.empty();
        }
    }

    public Mono<LoanApplication> findApplicationById(UUID applicationId, UserSnapshot userSnapshot) {
        return loanApplicationRepository.findById(applicationId)
                .filter(application -> canUserAccessApplication(application, userSnapshot))
                .switchIfEmpty(Mono.error(new SecurityException(
                    "Access denied to loan application: " + applicationId)));
    }

    public Mono<LoanApplication> save(LoanApplication loanApplication) {
        return validateUser(loanApplication.getUserId())
                .then(validateLoanType(loanApplication.getLoanTypeId()))
                .then(validateLoanAmount(loanApplication.getAmount()))
                .then(validateLoanTerm(loanApplication.getTermMonths()))
                .then(createLoanApplication(loanApplication))
                .flatMap(loanApplicationRepository::save);
    }

    public Mono<LoanApplication> findById(UUID id) {
        return loanApplicationRepository.findById(id);
    }

    public Flux<LoanApplication> findAll() {
        return loanApplicationRepository.findAll();
    }

    private Mono<Void> validateUser(String userId) {
        return userRepository.existsById(userId)
                .filter(exists -> exists)
                .switchIfEmpty(Mono.error(new IllegalArgumentException(
                    "El usuario con ID " + userId + " no existe en el sistema")))
                .then();
    }

    private Mono<Void> validateLoanType(UUID loanTypeId) {
        return loanTypeRepository.findById(loanTypeId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException(
                    "El tipo de préstamo especificado no existe")))
                .then();
    }

    private Mono<Void> validateLoanAmount(BigDecimal amount) {
        if (amount == null) {
            return Mono.error(new IllegalArgumentException("Amount cant no be null or empty"));
        }
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            return Mono.error(new IllegalArgumentException("Amount have to be more than zero"));
        }
        return Mono.empty();
    }

    private Mono<Void> validateLoanTerm(Integer termMonths) {
        if (termMonths == null) {
            return Mono.error(new IllegalArgumentException("term months can npt be null or empty"));
        }
        if (termMonths <= 0 || termMonths > 360) {
            return Mono.error(new IllegalArgumentException(
                "erm months have to be between 0 a 360"));
        }
        return Mono.empty();
    }

    private Mono<LoanApplication> createLoanApplication(LoanApplication loanApplication) {
        return Mono.just(LoanApplication.builder()
                .id(UUID.randomUUID())
                .userId(loanApplication.getUserId())
                .amount(loanApplication.getAmount())
                .termMonths(loanApplication.getTermMonths())
                .loanTypeId(loanApplication.getLoanTypeId())
                .status(ApplicationStatus.PENDING_REVIEW)
                .createdAt(LocalDateTime.now())
                .build());
    }

    private Mono<LoanApplication> createApplicationWithUserContext(LoanApplication application, UserSnapshot userSnapshot) {
        return Mono.just(LoanApplication.builder()
                .id(UUID.randomUUID())
                .userId(userSnapshot.id())
                .amount(application.getAmount())
                .termMonths(application.getTermMonths())
                .loanTypeId(application.getLoanTypeId())
                .status(ApplicationStatus.PENDING_REVIEW)
                .createdAt(LocalDateTime.now())
                .build());
    }

    private boolean canUserAccessApplication(LoanApplication application, UserSnapshot userSnapshot) {
        if (userSnapshot.canReviewApplications()) {
            return true;
        }
        
        if (userSnapshot.isClient()) {
            return application.getUserId().equals(userSnapshot.id());
        }
        
        return false;
    }
}