package com.aquaai.water.domain.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Types of water sources supplying irrigation water across Yangiariq district.
 */
@Getter
@RequiredArgsConstructor
public enum WaterSourceType {
    CANAL("Kanal", "Канал", "Canal"),
    DITCH("Ariq", "Арык", "Ditch"),
    RESERVOIR("Suv ombori", "Водохранилище", "Reservoir"),
    WELL("Quduq", "Скважина / Колодец", "Well"),
    RIVER("Daryo", "Река", "River"),
    OTHER("Boshqa", "Другое", "Other");

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
