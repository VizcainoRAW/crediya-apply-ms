package co.com.crediya.api;

import co.com.crediya.api.dto.ApiResponse;
import co.com.crediya.model.loanapplication.UserSnapshot;
import co.com.crediya.model.loanapplication.gateways.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import co.com.crediya.api.dto.LoanApplicationRequest;
import co.com.crediya.api.mapper.LoanApplicationMapper;
import co.com.crediya.usecase.loanapplication.LoanApplicationUseCase;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
@Slf4j
public class Handler {
    private final LoanApplicationUseCase useCase;
    private final LoanApplicationMapper mapper;
    private final UserRepository userRepository;

    public Mono<ServerResponse> createLoanApplication(ServerRequest request) {
        String clientIP = getClientIP(request);

        return extractAndValidateToken(request)
                .flatMap(userSnapshot -> {
                    log.info("Creating loan application - User: {}, Role: {}, IP: {}",
                            userSnapshot.id(), userSnapshot.role(), clientIP);

                    return request.bodyToMono(LoanApplicationRequest.class)
                            .doOnNext(req -> log.debug("Loan request details - Amount: {}, Term: {} months, LoanType: {}",
                                    req.amount(), req.termMonths(), req.loanTypeId()))
                            .map(mapper::toDomain)
                            .flatMap(app -> useCase.createLoanApplication(app, userSnapshot))
                            .map(savedApp -> {
                                log.info("Successfully created loan application: {} for user: {} - Amount: {}",
                                        savedApp.getId(), userSnapshot.id(), savedApp.getAmount());
                                return mapper.toResponse(savedApp);
                            });
                })
                .map(response -> ApiResponse.success(response, "Loan application created successfully"))
                .flatMap(apiResponse -> ServerResponse
                        .status(HttpStatus.CREATED)
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(apiResponse))
                .doOnError(error -> log.error("Error creating loan application from IP {}: {}",
                        clientIP, error.getMessage()))
                .onErrorResume(this::handleError);
    }

    public Mono<ServerResponse> getMyLoanApplications(ServerRequest request) {
        return extractAndValidateToken(request)
                .flatMap(userSnapshot -> {
                    log.info("Retrieving loan applications for user: {} with role: {}",
                            userSnapshot.id(), userSnapshot.role());

                    return useCase.findAllLoansByUser(userSnapshot)
                            .map(mapper::toResponse)
                            .collectList()
                            .doOnNext(applications -> log.info("Retrieved {} loan applications for user: {}",
                                    applications.size(), userSnapshot.id()));
                })
                .map(applications -> ApiResponse.success(applications,
                        "User loan applications retrieved successfully"))
                .flatMap(apiResponse -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(apiResponse))
                .doOnError(error -> log.error("Error retrieving user loan applications: {}",
                        error.getMessage()))
                .onErrorResume(this::handleError);
    }

    public Mono<ServerResponse> getPendingLoanApplications(ServerRequest request) {
        return extractAndValidateToken(request)
                .flatMap(userSnapshot -> {
                    log.info("Retrieving pending loan applications - Reviewer: {}, Role: {}",
                            userSnapshot.id(), userSnapshot.role());

                    return useCase.findAllPendingLoans(userSnapshot)
                            .map(mapper::toResponse)
                            .collectList()
                            .doOnNext(applications -> log.info("Retrieved {} pending loan applications for reviewer: {}",
                                    applications.size(), userSnapshot.id()));
                })
                .map(applications -> ApiResponse.success(applications,
                        "Pending loan applications retrieved successfully"))
                .flatMap(apiResponse -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(apiResponse))
                .doOnError(error -> log.error("Error retrieving pending loan applications: {}",
                        error.getMessage()))
                .onErrorResume(this::handleError);
    }

    private Mono<UserSnapshot> extractAndValidateToken(ServerRequest request) {
        return Mono.fromCallable(() -> {
                    String authHeader = request.headers().firstHeader(HttpHeaders.AUTHORIZATION);
                    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                        throw new SecurityException("Missing or invalid Authorization header");
                    }
                    return authHeader.substring(7);
                })
                .doOnNext(token -> log.debug("Extracted JWT token from request"))
                .flatMap(userRepository::validateTokenAndGetUser)
                .doOnNext(snapshot -> {
                    log.debug("Token validated - User: {}, Role: {}, Expires in: {}ms",
                            snapshot.id(), snapshot.role(), snapshot.tokenExpiresIn());
                });
    }

    private Mono<ServerResponse> handleError(Throwable throwable) {
        log.error("Handler error: {} - {}", throwable.getClass().getSimpleName(), throwable.getMessage());

        if (throwable instanceof SecurityException) {
            return ServerResponse.status(HttpStatus.UNAUTHORIZED)
                    .bodyValue(ApiResponse.error("Unauthorized: " + throwable.getMessage()));
        }

        if (throwable instanceof IllegalArgumentException) {
            return ServerResponse.badRequest()
                    .bodyValue(ApiResponse.error("Validation Error: " + throwable.getMessage()));
        }

        return ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .bodyValue(ApiResponse.error("Internal Server Error occurred"));
    }

    private String getClientIP(ServerRequest request) {
        String xForwardedFor = request.headers().firstHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.headers().firstHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        return request.remoteAddress()
                .map(address -> address.getAddress().getHostAddress())
                .orElse("unknown");
    }
}

