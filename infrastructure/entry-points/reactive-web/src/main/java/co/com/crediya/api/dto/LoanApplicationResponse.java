package co.com.crediya.api.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record LoanApplicationResponse(
        UUID id,
        String userId,
        UUID loanTypeId,
        BigDecimal amount,
        Integer termMonths,
        String status,
        LocalDateTime createdAt,
        LocalDateTime UpdatedAt
) {}
