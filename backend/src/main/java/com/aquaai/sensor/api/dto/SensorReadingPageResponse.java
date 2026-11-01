package com.aquaai.sensor.api.dto;

import com.aquaai.sensor.domain.model.SensorReadingPage;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Paginated list of telemetry readings")
public record SensorReadingPageResponse(
        List<SensorReadingResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last
) {
    public static SensorReadingPageResponse from(SensorReadingPage domainPage) {
        if (domainPage == null) {
            return null;
        }
        List<SensorReadingResponse> items = domainPage.content().stream()
                .map(SensorReadingResponse::from)
                .toList();

        return new SensorReadingPageResponse(
                items,
                domainPage.page(),
                domainPage.size(),
                domainPage.totalElements(),
                domainPage.totalPages(),
                domainPage.isFirst(),
                domainPage.isLast()
        );
    }
}
