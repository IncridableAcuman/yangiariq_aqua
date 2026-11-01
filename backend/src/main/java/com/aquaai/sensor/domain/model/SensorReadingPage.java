package com.aquaai.sensor.domain.model;

import java.util.List;

/**
 * Domain record encapsulating a paginated slice of {@link SensorReading} values.
 * Free of ORM and framework dependencies.
 */
public record SensorReadingPage(
        List<SensorReading> content,
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
