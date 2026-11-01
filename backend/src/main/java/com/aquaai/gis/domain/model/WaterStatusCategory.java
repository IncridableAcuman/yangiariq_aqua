package com.aquaai.gis.domain.model;

/**
 * Operational water status classification for agricultural parcels on GIS maps.
 */
public enum WaterStatusCategory {
    NORMAL,
    WARNING,
    OVER_CONSUMED,
    NO_ALLOCATION;

    public String getDisplayName(String lang) {
        String normalized = lang != null ? lang.toLowerCase() : "uz";
        return switch (normalized) {
            case "ru" -> switch (this) {
                case NORMAL -> "В норме";
                case WARNING -> "Внимание";
                case OVER_CONSUMED -> "Перерасход";
                case NO_ALLOCATION -> "Лимит не выделен";
            };
            case "en" -> switch (this) {
                case NORMAL -> "Normal";
                case WARNING -> "Warning";
                case OVER_CONSUMED -> "Over-Consumed";
                case NO_ALLOCATION -> "No Allocation";
            };
            default -> switch (this) {
                case NORMAL -> "Normal";
                case WARNING -> "Ogohlantirish";
                case OVER_CONSUMED -> "Meʼyordan oshgan";
                case NO_ALLOCATION -> "Limít ajratilmagan";
            };
        };
    }
}
