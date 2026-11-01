package com.aquaai.irrigation.domain.service;

import com.aquaai.irrigation.domain.model.DataQualityGate;
import com.aquaai.irrigation.domain.model.IrrigationPriority;

import java.math.BigDecimal;

/**
 * Deterministic priority calculator computing an urgency score (0 - 100)
 * based on soil moisture deficit, crop sensitivity, meteorological stress,
 * and quota availability.
 */
public class IrrigationPriorityCalculator {

    public record PriorityResult(
            int score,
            IrrigationPriority priority,
            String reasoning
    ) {}

    public PriorityResult calculatePriority(
            BigDecimal currentMoisturePercent,
            BigDecimal targetMoisturePercent,
            String cropType,
            boolean hasCriticalMoistureAnomaly,
            BigDecimal ambientTemperatureCelsius,
            BigDecimal forecastRainMm,
            BigDecimal remainingWaterQuotaM3,
            BigDecimal requiredVolumeM3,
            DataQualityGate qualityGate
    ) {
        int score = 0;
        StringBuilder reasoning = new StringBuilder();

        // 1. Soil Moisture Deficit Score (0 to 40 pts)
        if (currentMoisturePercent != null && targetMoisturePercent != null) {
            BigDecimal gap = targetMoisturePercent.subtract(currentMoisturePercent);
            if (gap.compareTo(BigDecimal.valueOf(25.0)) >= 0) {
                score += 40;
                reasoning.append("Severe soil moisture deficit (>25% below target); ");
            } else if (gap.compareTo(BigDecimal.valueOf(15.0)) >= 0) {
                score += 30;
                reasoning.append("Substantial soil moisture deficit (15-25% below target); ");
            } else if (gap.compareTo(BigDecimal.valueOf(5.0)) >= 0) {
                score += 20;
                reasoning.append("Moderate soil moisture deficit (5-15% below target); ");
            } else if (gap.compareTo(BigDecimal.ZERO) > 0) {
                score += 10;
                reasoning.append("Mild soil moisture deficit (<5% below target); ");
            } else {
                reasoning.append("Soil moisture at or above target; ");
            }
        } else {
            // Missing moisture telemetry
            score += 15;
            reasoning.append("Moisture telemetry unavailable (baseline priority assigned); ");
        }

        // 2. Crop Sensitivity Score (0 to 25 pts)
        int cropPts = resolveCropSensitivityPoints(cropType);
        score += cropPts;
        if (cropPts >= 20) {
            reasoning.append("High-sensitivity crop (").append(cropType).append("); ");
        }

        // 3. Anomaly / Agronomic Stress State (0 to 15 pts)
        if (hasCriticalMoistureAnomaly) {
            score += 15;
            reasoning.append("Active SOIL_MOISTURE_CRITICAL alert; ");
        }

        // 4. Meteorological Stress (0 to 10 pts)
        if (ambientTemperatureCelsius != null && ambientTemperatureCelsius.compareTo(BigDecimal.valueOf(38.0)) >= 0) {
            score += 10;
            reasoning.append("Extreme ambient heat stress (").append(ambientTemperatureCelsius).append("°C); ");
        } else if (ambientTemperatureCelsius != null && ambientTemperatureCelsius.compareTo(BigDecimal.valueOf(32.0)) >= 0) {
            score += 5;
            reasoning.append("Elevated ambient temperature; ");
        }

        // Rain deduction: if heavy rain is forecast soon, drop priority!
        if (forecastRainMm != null && forecastRainMm.compareTo(BigDecimal.valueOf(10.0)) >= 0) {
            score = Math.max(0, score - 25);
            reasoning.append("Significant rain forecast (").append(forecastRainMm).append(" mm) reduces urgency; ");
        }

        // 5. Quota Headroom / Water Availability (0 to 10 pts)
        if (remainingWaterQuotaM3 != null && requiredVolumeM3 != null) {
            if (remainingWaterQuotaM3.compareTo(requiredVolumeM3) >= 0) {
                score += 10;
            } else if (remainingWaterQuotaM3.compareTo(BigDecimal.ZERO) > 0) {
                score += 5;
                reasoning.append("Partial water quota available; ");
            } else {
                reasoning.append("Water quota depleted; ");
            }
        }

        // 6. Quality Gate: Low-quality/uncalibrated sensors must NOT produce critical priority
        if (qualityGate == DataQualityGate.INSUFFICIENT || qualityGate == DataQualityGate.UNSAFE) {
            score = Math.min(score, 50); // Cap at MEDIUM
            reasoning.append("Priority score capped due to degraded/unreliable telemetry quality; ");
        }

        // Clamp final score to 0..100
        score = Math.max(0, Math.min(100, score));
        IrrigationPriority priority = IrrigationPriority.fromScore(score);

        return new PriorityResult(score, priority, reasoning.toString().trim());
    }

    private int resolveCropSensitivityPoints(String cropType) {
        if (cropType == null) {
            return 10;
        }
        return switch (cropType.toUpperCase()) {
            case "COTTON", "RICE" -> 25;
            case "VEGETABLE", "MELON" -> 20;
            case "MAIZE", "WHEAT" -> 15;
            default -> 10;
        };
    }
}
