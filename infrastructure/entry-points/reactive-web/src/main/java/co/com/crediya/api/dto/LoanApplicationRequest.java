package co.com.crediya.api.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record LoanApplicationRequest(
        UUID loanTypeId,
        BigDecimal amount,
        Integer termMonths
) {}