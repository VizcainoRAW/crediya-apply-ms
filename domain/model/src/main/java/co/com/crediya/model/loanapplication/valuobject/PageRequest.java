package co.com.crediya.model.loanapplication.valuobject;

public record PageRequest (
        int page,
        int size,
        String sortBy,
        String sortDirection
){
    public PageRequest {
        if (page < 0) {
            throw new IllegalArgumentException("Page number cannot be negative");
        }
        if (size <= 0) {
            throw new IllegalArgumentException("Page size must be positive");
        }
        if (size > 100) {
            throw new IllegalArgumentException("Page size cannot exceed 100");
        }

        sortBy = (sortBy == null || sortBy.trim().isEmpty()) ? "createdAt" : sortBy;
        sortDirection = (sortDirection == null || sortDirection.trim().isEmpty()) ? "DESC" : sortDirection.toUpperCase();

        if (!"ASC".equals(sortDirection) && !"DESC".equals(sortDirection)) {
            throw new IllegalArgumentException("Sort direction must be ASC or DESC");
        }
    }

    public static PageRequest of(int page, int size) {
        return new PageRequest(page, size, "createdAt", "DESC");
    }

    public static PageRequest of(int page, int size, String sortBy, String sortDirection) {
        return new PageRequest(page, size, sortBy, sortDirection);
    }

    public int offset() {
        return page * size;
    }
}