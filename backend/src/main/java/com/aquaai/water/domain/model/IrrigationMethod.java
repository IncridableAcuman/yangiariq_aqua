package com.aquaai.water.domain.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Irrigation techniques utilized in water planning and delivery events across Yangiariq.
 * Semantically aligned with field irrigation classifications.
 */
@Getter
@RequiredArgsConstructor
public enum IrrigationMethod {
    CANAL("Kanal orqali", "Канальное", "Canal"),
    DITCH("Ariq orqali", "Арычное", "Ditch"),
    FURROW("Egatlab sug'orish", "Бороздковое", "Furrow"),
    DRIP("Tomchilatib sug'orish", "Капельное", "Drip"),
    SPRINKLER("Yomg'irlatib sug'orish", "Дождевание", "Sprinkler"),
    OTHER("Boshqa", "Другой", "Other");

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
