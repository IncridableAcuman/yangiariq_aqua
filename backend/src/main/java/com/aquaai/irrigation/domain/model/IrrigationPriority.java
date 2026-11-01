package com.aquaai.irrigation.domain.model;

/**
 * Priority classification for parcel irrigation dispatch.
 */
public enum IrrigationPriority {
    CRITICAL(80),
    HIGH(60),
    MEDIUM(35),
    LOW(0);

    private final int minimumScore;

    IrrigationPriority(int minimumScore) {
        this.minimumScore = minimumScore;
    }

    public int getMinimumScore() {
        return minimumScore;
    }

    public static IrrigationPriority fromScore(int score) {
        if (score >= CRITICAL.minimumScore) {
            return CRITICAL;
        } else if (score >= HIGH.minimumScore) {
            return HIGH;
        } else if (score >= MEDIUM.minimumScore) {
            return MEDIUM;
        } else {
            return LOW;
        }
    }
}
