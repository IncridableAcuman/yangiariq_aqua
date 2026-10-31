package com.aquaai.water.domain.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Execution status of an actual irrigation delivery event.
 */
@Getter
@RequiredArgsConstructor
public enum IrrigationEventStatus {
    STARTED("Boshlangan", "Начато", "Started"),
    COMPLETED("Tugallangan", "Завершено", "Completed"),
    CANCELLED("Bekor qilingan", "Отменено", "Cancelled");

    private final String nameUz;
    private final String nameRu;
    private final String nameEn;

    public String getDisplayName(String languageCode) {
        if (languageCode == null) {
            return nameUz;
        }
        return switch (languageCode.toLowerCase()) {
            case "ru" -> nameRu;
            case "en" -> nameEn;
            default -> nameUz;
        };
    }
}
