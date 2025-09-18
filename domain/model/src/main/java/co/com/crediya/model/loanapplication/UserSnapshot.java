package co.com.crediya.model.loanapplication;

import java.math.BigDecimal;
import java.time.LocalDate;

public record UserSnapshot(
    String id,
    String firstName,
    String lastName,
    LocalDate birthDate,
    String address,
    String phone,
    String email,
    BigDecimal baseSalary,
    String role,
    String documentType,
    String documentId,
    Long tokenExpiresIn
) {
    public UserSnapshot(String id, String role, Long tokenExpiresIn) {
        this(id, null, null, null, null, null, null, null, role, null, null, tokenExpiresIn);
    }

    public boolean isAdmin() {
        return "ADMIN".equalsIgnoreCase(role);
    }
    
    public boolean isAdvisor() {
        return "ADVISOR".equalsIgnoreCase(role);
    }
    
    public boolean isClient() {
        return "USER".equalsIgnoreCase(role);
    }
    
    public boolean canReviewApplications() {
        return isAdmin() || isAdvisor();
    }
}