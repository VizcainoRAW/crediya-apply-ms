package co.com.crediya.model.loanapplication;

public enum ApplicationStatus {
    PENDING_REVIEW("PENDING", "Applicantion pending for a review"),
    MANUAL_REVIEW("MANUAL","Application pending for a manual review"),
    ACCEPTED("ACCEPTED", "Application accepted"),
    CANCELLED("CANCELLED","Application canceled");

    private final String code;
    private final String description;

    ApplicationStatus(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static ApplicationStatus fromCode(String code) {
        for (ApplicationStatus applicationStatus : ApplicationStatus.values()) {
            if (applicationStatus.code.equals(code)) {
                return applicationStatus;
            }
        }
        throw new IllegalArgumentException("Unknown role code: " + code);
    }
}