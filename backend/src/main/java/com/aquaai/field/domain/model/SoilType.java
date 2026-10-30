package com.aquaai.field.domain.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Type-safe soil classification for agricultural field parcels in the Khorezm oasis.
 */
@Getter
@RequiredArgsConstructor
public enum SoilType {
    SANDY("Qumli", "Песчаная", "Sandy"),
    LOAM("Qumoq (bo'z)", "Суглинистая", "Loam"),
    CLAY("Loyli", "Глинистая", "Clay"),
    SANDY_LOAM("Qumloq", "Супесчаная", "Sandy Loam"),
    CLAY_LOAM("Og'ir qumoq", "Тяжелосуглинистая", "Clay Loam"),
    OTHER("Boshqa", "Другая", "Other");

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
