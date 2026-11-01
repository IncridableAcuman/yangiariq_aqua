package com.aquaai.anomaly.infrastructure.detection;

import com.aquaai.anomaly.domain.detection.AnomalyDetector;
import com.aquaai.anomaly.domain.detection.DetectionContext;
import com.aquaai.anomaly.domain.model.AnomalyCandidate;
import com.aquaai.anomaly.domain.model.AnomalyEvidence;
import com.aquaai.anomaly.domain.model.AnomalySeverity;
import com.aquaai.anomaly.domain.model.AnomalyType;
import com.aquaai.sensor.domain.model.ReadingQuality;
import com.aquaai.sensor.domain.model.SensorType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Deterministic rule detector for soil/water salinity (Electrical Conductivity).
 * Crucial for Aral Sea basin & Khorezm oasis saline soils management.
 */
@Component
public class SalinityRuleDetector implements AnomalyDetector {

    public static final BigDecimal DEFAULT_HIGH_SALINITY = BigDecimal.valueOf(4.0); // 4.0 dS/m
    public static final BigDecimal DEFAULT_SEVERE_SALINITY = BigDecimal.valueOf(6.5); // 6.5 dS/m

    @Override
    public boolean supports(SensorType sensorType) {
        return sensorType == SensorType.SALINITY;
    }

    @Override
    public List<AnomalyCandidate> detect(DetectionContext context) {
        BigDecimal value = context.getCurrentReadingValue();
        if (value == null || context.getCurrentReadingQuality() == ReadingQuality.INVALID) {
            return Collections.emptyList();
        }

        List<AnomalyCandidate> candidates = new ArrayList<>();
        Instant now = context.getCurrentReadingTime();

        BigDecimal threshold = context.getRuleConfig() != null && context.getRuleConfig().getThreshold() != null
                ? context.getRuleConfig().getThreshold()
                : DEFAULT_HIGH_SALINITY;

        if (value.compareTo(threshold) > 0) {
            boolean isSevere = value.compareTo(DEFAULT_SEVERE_SALINITY) >= 0;
            AnomalySeverity severity = isSevere ? AnomalySeverity.CRITICAL : AnomalySeverity.HIGH;
            BigDecimal expected = BigDecimal.valueOf(2.0);
            BigDecimal deviation = value.subtract(expected).setScale(2, RoundingMode.HALF_UP);

            candidates.add(new AnomalyCandidate(
                    AnomalyType.SALINITY_INCREASE,
                    severity,
                    value,
                    expected,
                    deviation,
                    BigDecimal.valueOf(0.90),
                    isSevere ? "Oʻta yuqori tuproq shoʻrlanishi (Zararlanish xavfi)" : "Tuproq / suv shoʻrlanishining ortishi",
                    String.format("Shoʻrlanish darajasi %.2f dS/m ga yetdi (meʼyor: < %.1f dS/m). Xorazm tuproq sharoitida ildiz zonasida tuz toʻplanishi va shoʻr yuvish zarurati belgilari kuzatilmoqda.",
                            value.doubleValue(), threshold.doubleValue()),
                    "RULE_ENGINE",
                    List.of(AnomalyEvidence.of(
                            "SALINITY_BREACH",
                            "SALINITY",
                            value,
                            threshold,
                            now,
                            "{\"salinityThreshold\": " + threshold + "}"
                    )),
                    context.isSimulated()
            ));
        }

        return candidates;
    }
}
