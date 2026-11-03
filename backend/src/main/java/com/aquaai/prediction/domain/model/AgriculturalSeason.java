package com.aquaai.prediction.domain.model;

import java.time.LocalDate;
import java.time.Month;

/**
 * Seasonal agricultural periods for Yangiariq (Khorezm, Uzbekistan) continental climate.
 */
public enum AgriculturalSeason {
    SPRING(Month.MARCH, Month.APRIL, Month.MAY),
    SUMMER(Month.JUNE, Month.JULY, Month.AUGUST),
    AUTUMN(Month.SEPTEMBER, Month.OCTOBER, Month.NOVEMBER),
    WINTER(Month.DECEMBER, Month.JANUARY, Month.FEBRUARY);

    private final Month[] months;

    AgriculturalSeason(Month... months) {
        this.months = months;
    }

    public static AgriculturalSeason fromMonth(int month) {
        return switch (month) {
            case 3, 4, 5 -> SPRING;
            case 6, 7, 8 -> SUMMER;
            case 9, 10, 11 -> AUTUMN;
            default -> WINTER;
        };
    }

    public static AgriculturalSeason fromDate(LocalDate date) {
        if (date == null) return SUMMER;
        return fromMonth(date.getMonthValue());
    }

    public static AgriculturalSeason fromInstant(java.time.Instant instant) {
        if (instant == null) return SUMMER;
        return fromDate(instant.atZone(java.time.ZoneOffset.UTC).toLocalDate());
    }
}
