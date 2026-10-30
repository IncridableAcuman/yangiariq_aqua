package com.aquaai.shared.pagination;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public final class PaginationUtils {

    public static final int DEFAULT_PAGE_NUMBER = 0;
    public static final int DEFAULT_PAGE_SIZE = 20;
    public static final int MAX_PAGE_SIZE = 100;

    private PaginationUtils() {
    }

    public static Pageable createPageRequest(int page, int size, String sortBy, String direction) {
        int validatedPage = Math.max(page, DEFAULT_PAGE_NUMBER);
        int validatedSize = (size <= 0) ? DEFAULT_PAGE_SIZE : Math.min(size, MAX_PAGE_SIZE);

        Sort.Direction sortDirection = "desc".equalsIgnoreCase(direction)
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;

        String safeSortBy = (sortBy != null && !sortBy.isBlank()) ? sortBy : "createdAt";

        return PageRequest.of(validatedPage, validatedSize, Sort.by(sortDirection, safeSortBy));
    }
}
