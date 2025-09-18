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
    public boolean isAdmin() {
        return "ADMIN".equalsIgnoreCase(role);
    }
    
    public boolean isAsesor() {
        return "ASESOR".equalsIgnoreCase(role);
    }
    
    public boolean isClient() {
        return "CLIENT".equalsIgnoreCase(role);
    }
    
    public boolean canReviewApplications() {
        return isAdmin() || isAsesor();
    }
}