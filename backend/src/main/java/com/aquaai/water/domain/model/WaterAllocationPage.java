package com.aquaai.water.domain.model;

import java.util.List;

/**
 * Domain record encapsulating a paginated slice of {@link WaterAllocation} aggregates.
 */
public record WaterAllocationPage(
        List<WaterAllocation> content,
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
