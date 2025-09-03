package co.com.crediya.model.loanapplication;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class LoanApplication {
    private UUID id;
    private String userId;
    private UUID loanTypeId;
    private BigDecimal amount;
    private Integer termMonths;
    private ApplicationStatus status;
    private LocalDateTime createdAt;
}