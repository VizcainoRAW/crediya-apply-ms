package co.com.crediya.model.loanapplication;

public record UserSnapshot(
    String id,
    String role,
    String documentType,
    String documentId 
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