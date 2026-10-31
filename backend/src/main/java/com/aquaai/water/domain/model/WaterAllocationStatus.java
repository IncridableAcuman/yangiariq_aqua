package com.aquaai.water.domain.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Status of a volumetric water allocation assigned to an agricultural parcel.
 */
@Getter
@RequiredArgsConstructor
public enum WaterAllocationStatus {
    ACTIVE("Faol", "Активное", "Active"),
    CLOSED("Yopilgan", "Закрытое", "Closed"),
    CANCELLED("Bekor qilingan", "Отмененное", "Cancelled");

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
