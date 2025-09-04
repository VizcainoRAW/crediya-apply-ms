package co.com.crediya.api.mapper;

import org.springframework.stereotype.Component;

import co.com.crediya.api.dto.LoanApplicationRequest;
import co.com.crediya.api.dto.LoanApplicationResponse;
import co.com.crediya.model.loanapplication.LoanApplication;

@Component
public class LoanApplicationMapper {

    public LoanApplication toDomain(LoanApplicationRequest dto) {
        return LoanApplication.builder()
                .userId(dto.userId())
                .loanTypeId(dto.loanTypeId())
                .amount(dto.amount())
                .termMonths(dto.termMonths())
                .build();
    }

    public LoanApplicationResponse toResponse(LoanApplication domain) {
        return new LoanApplicationResponse(
                domain.getId(),
                domain.getUserId(),
                domain.getLoanTypeId(),
                domain.getAmount(),
                domain.getTermMonths(),
                domain.getStatus().name(),
                domain.getCreatedAt()
        );
    }
}