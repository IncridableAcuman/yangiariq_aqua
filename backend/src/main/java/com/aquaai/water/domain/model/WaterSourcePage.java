package com.aquaai.water.domain.model;

import java.util.List;

/**
 * Domain record encapsulating a paginated slice of {@link WaterSource} aggregates.
 * Free of ORM and framework dependencies.
 */
public record WaterSourcePage(
        List<WaterSource> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
    public boolean isFirst() {
        return page == 0;
    }

    public boolean isLast() {
        return page >= totalPages - 1;
    }
}
