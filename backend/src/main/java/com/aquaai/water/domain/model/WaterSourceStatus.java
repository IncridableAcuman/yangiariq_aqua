package com.aquaai.water.domain.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Operational status of a regional water source infrastructure entity.
 */
@Getter
@RequiredArgsConstructor
public enum WaterSourceStatus {
    ACTIVE("Faol", "Активный", "Active"),
    INACTIVE("Nofaol", "Неактивный", "Inactive");

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
