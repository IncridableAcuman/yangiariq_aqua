package com.aquaai.field.domain.model;

import java.util.List;

/**
 * Domain-level representation of a paginated collection of {@link Field} aggregates.
 * Keeps domain abstractions independent of Spring Data's {@code Page<T>}.
 */
public record FieldPage(
        List<Field> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
    public boolean isFirst() {
        return page == 0;
    }

    public boolean isLast() {
        return page >= totalPages - 1 || totalPages == 0;
    }
}
