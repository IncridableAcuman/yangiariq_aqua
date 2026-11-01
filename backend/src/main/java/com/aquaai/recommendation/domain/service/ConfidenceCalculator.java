package com.aquaai.recommendation.domain.service;

import com.aquaai.recommendation.domain.model.AgronomicRecommendationContext;
import com.aquaai.sensor.domain.model.ReadingQuality;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;

/**
 * Multi-factor calibration service for deriving bounded, evidence-aware confidence scores.
 * <p>
 * Formula:
 * Confidence = clamp(0.10, 0.95, w_e * E + w_q * Q + w_c * C + w_a * A)
 * <ul>
 *   <li>w_e = 0.40 (Evidence Strength: number and corroborated consistency of physical metrics)</li>
 *   <li>w_q = 0.25 (Data Quality & Freshness: age of sensor observations and VALID quality flag)</li>
 *   <li>w_c = 0.20 (Context Completeness: presence of crop, soil, weather, and hydraulic data)</li>
 *   <li>w_a = 0.15 (Model Assessment factor)</li>
 * </ul>
 */
public class ConfidenceCalculator {

    private static final BigDecimal W_EVIDENCE = BigDecimal.valueOf(0.40);
    private static final BigDecimal W_QUALITY = BigDecimal.valueOf(0.25);
    private static final BigDecimal W_COMPLETENESS = BigDecimal.valueOf(0.20);
    private static final BigDecimal W_ASSESSMENT = BigDecimal.valueOf(0.15);

    public BigDecimal calculate(AgronomicRecommendationContext context, int evidenceCount, BigDecimal modelConfidence) {
        if (context == null) {
            return BigDecimal.valueOf(0.50);
        }

        // 1. Evidence strength score (E: 0.2 to 1.0 based on evidence count)
        double evidenceScore = Math.min(1.0, 0.3 + (evidenceCount * 0.25));

        // 2. Data quality & freshness score (Q)
        double qualityScore = 0.5;
        if (context.getSoilMoistureQuality() == ReadingQuality.VALID) {
            qualityScore = 0.9;
        } else if (context.getSoilMoistureQuality() == ReadingQuality.SUSPECT) {
            qualityScore = 0.4;
        }

        if (context.getWeather() != null && context.getWeather().isFresh(Instant.now(), 180)) {
            qualityScore = Math.min(1.0, qualityScore + 0.1);
        }

        // 3. Context completeness score (C)
        int presentFields = 0;
        int totalExpected = 6;
        if (context.getCropType() != null) presentFields++;
        if (context.getSoilType() != null) presentFields++;
        if (context.getFieldAreaHectares() != null) presentFields++;
        if (context.getSoilMoisturePercent() != null) presentFields++;
        if (context.getWeather() != null) presentFields++;
        if (context.getIrrigationMethod() != null) presentFields++;
        double completenessScore = (double) presentFields / totalExpected;

        // 4. Model assessment score (A)
        double assessmentScore = modelConfidence != null ? modelConfidence.doubleValue() : 0.80;
        assessmentScore = Math.max(0.1, Math.min(1.0, assessmentScore));

        // Weighted summation
        double composite = (W_EVIDENCE.doubleValue() * evidenceScore)
                + (W_QUALITY.doubleValue() * qualityScore)
                + (W_COMPLETENESS.doubleValue() * completenessScore)
                + (W_ASSESSMENT.doubleValue() * assessmentScore);

        BigDecimal bounded = BigDecimal.valueOf(composite).setScale(4, RoundingMode.HALF_UP);
        if (bounded.compareTo(BigDecimal.valueOf(0.10)) < 0) {
            return BigDecimal.valueOf(0.10);
        }
        if (bounded.compareTo(BigDecimal.valueOf(0.95)) > 0) {
            return BigDecimal.valueOf(0.95);
        }
        return bounded;
    }
}
