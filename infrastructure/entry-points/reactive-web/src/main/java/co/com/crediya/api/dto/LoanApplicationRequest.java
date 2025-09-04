package co.com.crediya.api.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record LoanApplicationRequest(
        String userId,
        UUID loanTypeId,
        BigDecimal amount,
        Integer termMonths
) {}