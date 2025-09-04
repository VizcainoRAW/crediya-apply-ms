package co.com.crediya.api;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

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

    public Mono<ServerResponse> createLoanApplication(ServerRequest request) {
        return request.bodyToMono(LoanApplicationRequest.class)
                .doOnNext(req -> log.info("Incoming request to create loan application: {}", req))
                .map(mapper::toDomain)
                .flatMap(useCase::save)
                .map(mapper::toResponse)
                .flatMap(dto -> {
                    log.info("Successfully created loan application: {}", dto);
                    return ServerResponse
                            .status(HttpStatus.CREATED)
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(dto);
                })
                .doOnError(error -> log.error("Error creating loan application: {}", error.getMessage()));
    }

    public Mono<ServerResponse> getLoanApplication(ServerRequest request) {
        UUID id = UUID.fromString(request.pathVariable("id"));
        log.info("Incoming request to get loan application with id: {}", id);
        return useCase.findById(id)
                .map(mapper::toResponse)
                .flatMap(dto -> {
                    log.info("Successfully found loan application: {}", dto);
                    return ServerResponse.ok()
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(dto);
                })
                .switchIfEmpty(Mono.defer(() -> {
                    log.warn("Loan application with id: {} not found", id);
                    return ServerResponse.notFound().build();
                }))
                .doOnError(error -> log.error("Error getting loan application with id: {}: {}", id, error.getMessage()));
    }

    public Mono<ServerResponse> listLoanApplications(ServerRequest request) {
        log.info("Incoming request to list all loan applications");
        return useCase.findAll()
                .map(mapper::toResponse)
                .collectList()
                .flatMap(list -> {
                    log.info("Successfully retrieved {} loan applications", list.size());
                    return ServerResponse.ok()
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(list);
                })
                .doOnError(error -> log.error("Error listing loan applications: {}", error.getMessage()));
    }
}