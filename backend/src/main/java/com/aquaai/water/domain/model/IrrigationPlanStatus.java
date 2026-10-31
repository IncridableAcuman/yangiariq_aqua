package com.aquaai.water.domain.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Lifecycle status of a planned irrigation run.
 */
@Getter
@RequiredArgsConstructor
public enum IrrigationPlanStatus {
    PLANNED("Rejalashtirilgan", "Запланировано", "Planned"),
    IN_PROGRESS("Jarayonda", "В процессе", "In Progress"),
    COMPLETED("Bajarildi", "Завершено", "Completed"),
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
