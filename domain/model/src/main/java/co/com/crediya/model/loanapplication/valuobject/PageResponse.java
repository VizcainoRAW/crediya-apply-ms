package co.com.crediya.model.loanapplication.valuobject;

import java.util.List;

public record PageResponse<T>(
        List<T> content,
        int totalElements,
        int currentPage,
        int size
) {
}
