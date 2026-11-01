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
 * Deterministic rule detector for soil moisture observations.
 * Evaluates critical moisture deficit, rapid decline, and moisture thresholds.
 * Thresholds represent provisional engineering baselines configurable per crop/soil profile.
 */
@Component
public class SoilMoistureRuleDetector implements AnomalyDetector {

    public static final BigDecimal DEFAULT_CRITICAL_THRESHOLD = BigDecimal.valueOf(15.0); // 15%
    public static final BigDecimal DEFAULT_WARNING_THRESHOLD = BigDecimal.valueOf(25.0);  // 25%
    public static final BigDecimal DEFAULT_DROP_RATE_THRESHOLD = BigDecimal.valueOf(10.0); // 10% drop in window

    @Override
    public boolean supports(SensorType sensorType) {
        return sensorType == SensorType.SOIL_MOISTURE;
    }

    @Override
    public List<AnomalyCandidate> detect(DetectionContext context) {
        BigDecimal value = context.getCurrentReadingValue();
        if (value == null || context.getCurrentReadingQuality() == ReadingQuality.INVALID) {
            return Collections.emptyList();
        }

        List<AnomalyCandidate> candidates = new ArrayList<>();
        Instant now = context.getCurrentReadingTime();

        BigDecimal criticalThreshold = context.getRuleConfig() != null && context.getRuleConfig().getThreshold() != null
                ? context.getRuleConfig().getThreshold()
                : DEFAULT_CRITICAL_THRESHOLD;

        BigDecimal warningThreshold = context.getRuleConfig() != null && context.getRuleConfig().getSecondaryThreshold() != null
                ? context.getRuleConfig().getSecondaryThreshold()
                : DEFAULT_WARNING_THRESHOLD;

        // 1. Critical soil moisture check
        if (value.compareTo(criticalThreshold) < 0) {
            BigDecimal deviation = value.subtract(criticalThreshold).setScale(2, RoundingMode.HALF_UP);
            candidates.add(new AnomalyCandidate(
                    AnomalyType.SOIL_MOISTURE_CRITICAL,
                    AnomalySeverity.CRITICAL,
                    value,
                    criticalThreshold,
                    deviation,
                    BigDecimal.valueOf(0.92),
                    "Oʻta past tuproq namligi (Kritik defitsit)",
                    String.format("Tuproq namligi %.1f%% gacha pasayib, kritik boʻsagʻadan (%.1f%%) pastga tushdi. Ekin qurishi va hosil yoʻqotish xavfi mavjud.",
                            value.doubleValue(), criticalThreshold.doubleValue()),
                    "RULE_ENGINE",
                    List.of(AnomalyEvidence.of(
                            "THRESHOLD_BREACH",
                            "SOIL_MOISTURE",
                            value,
                            criticalThreshold,
                            now,
                            "{\"provisionalThreshold\": " + criticalThreshold + ", \"crop\": \"" + context.getCropType() + "\"}"
                    )),
                    context.isSimulated()
            ));
            return candidates; // Critical takes precedence
        }

        // 2. Rapid moisture decline check
        if (!context.getRecentReadings().isEmpty()) {
            DetectionContext.HistoricalReadingPoint prev = context.getRecentReadings().get(0);
            if (prev.value() != null && prev.quality() == ReadingQuality.VALID) {
                BigDecimal drop = prev.value().subtract(value);
                if (drop.compareTo(DEFAULT_DROP_RATE_THRESHOLD) > 0) {
                    BigDecimal deviation = drop.negate().setScale(2, RoundingMode.HALF_UP);
                    candidates.add(new AnomalyCandidate(
                            AnomalyType.SOIL_MOISTURE_DROP,
                            AnomalySeverity.HIGH,
                            value,
                            prev.value(),
                            deviation,
                            BigDecimal.valueOf(0.85),
                            "Tuproq namligining keskin pasayishi",
                            String.format("Tuproq namligi qisqa vaqt ichida %.1f%% dan %.1f%% ga keskin pasaydi (%.1f%% yoʻqotish). Dala qurgʻoqchiligi yoki drenaj nosozligi tekshirilishi lozim.",
                                    prev.value().doubleValue(), value.doubleValue(), drop.doubleValue()),
                            "RULE_ENGINE",
                            List.of(
                                    AnomalyEvidence.of("RATE_OF_CHANGE", "SOIL_MOISTURE_PREV", prev.value(), null, prev.measuredAt(), null),
                                    AnomalyEvidence.of("RATE_OF_CHANGE", "SOIL_MOISTURE_CURRENT", value, null, now, null)
                            ),
                            context.isSimulated()
                    ));
                    return candidates;
                }
            }
        }

        // 3. Warning low moisture check
        if (value.compareTo(warningThreshold) < 0) {
            BigDecimal deviation = value.subtract(warningThreshold).setScale(2, RoundingMode.HALF_UP);
            candidates.add(new AnomalyCandidate(
                    AnomalyType.SOIL_MOISTURE_DROP,
                    AnomalySeverity.MEDIUM,
                    value,
                    warningThreshold,
                    deviation,
                    BigDecimal.valueOf(0.80),
                    "Tuproq namligi meʼyordan past",
                    String.format("Tuproq namligi %.1f%% boʻlib, agronomik ogohlantirish meʼyoridan (%.1f%%) past.",
                            value.doubleValue(), warningThreshold.doubleValue()),
                    "RULE_ENGINE",
                    List.of(AnomalyEvidence.of(
                            "WARNING_THRESHOLD",
                            "SOIL_MOISTURE",
                            value,
                            warningThreshold,
                            now,
                            "{\"warningThreshold\": " + warningThreshold + "}"
                    )),
                    context.isSimulated()
            ));
        }

        return candidates;
    }
}
