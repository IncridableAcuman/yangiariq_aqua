package com.aquaai.field.domain.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Agricultural crop classification tailored to the Aral Sea basin and Khorezm / Yangiariq district.
 */
@Getter
@RequiredArgsConstructor
public enum CropType {
    COTTON("Paxta", "Хлопчатник", "Cotton"),
    WHEAT("Bug'doy", "Пшеница", "Wheat"),
    RICE("Sholi", "Рис", "Rice"),
    MAIZE("Makkajo'xori", "Кукуруза", "Maize"),
    VEGETABLE("Sabzavotlar", "Овощи", "Vegetable"),
    MELON("Poliz ekinlari", "Бахчевые", "Melon"),
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
