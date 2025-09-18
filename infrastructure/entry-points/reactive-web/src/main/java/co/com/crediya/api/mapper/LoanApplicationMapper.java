package co.com.crediya.api.mapper;

import org.springframework.stereotype.Component;

import co.com.crediya.api.dto.LoanApplicationRequest;
import co.com.crediya.api.dto.LoanApplicationResponse;
import co.com.crediya.model.loanapplication.LoanApplication;

@Component
public class LoanApplicationMapper {

    public LoanApplication toDomain(LoanApplicationRequest request) {
        return LoanApplication.builder()
                .amount(request.amount())
                .termMonths(request.termMonths())
                .loanTypeId(request.loanTypeId())
                .build();
    }

    public LoanApplicationResponse toResponse(LoanApplication domain) {
        return new LoanApplicationResponse(
                domain.getId(),
                domain.getUserId(),
                domain.getLoanTypeId(),
                domain.getAmount(),
                domain.getTermMonths(),
                domain.getStatus().getCode(),
                domain.getCreatedAt(),
                domain.getUpdatedAt()
        );
    }
}