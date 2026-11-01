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
 * Deterministic rule detector for ambient temperature (frost vs extreme heat).
 */
@Component
public class TemperatureRuleDetector implements AnomalyDetector {

    public static final BigDecimal FROST_THRESHOLD = BigDecimal.valueOf(2.0);    // < 2°C
    public static final BigDecimal EXTREME_HEAT_THRESHOLD = BigDecimal.valueOf(42.0); // > 42°C

    @Override
    public boolean supports(SensorType sensorType) {
        return sensorType == SensorType.TEMPERATURE;
    }

    @Override
    public List<AnomalyCandidate> detect(DetectionContext context) {
        BigDecimal value = context.getCurrentReadingValue();
        if (value == null || context.getCurrentReadingQuality() == ReadingQuality.INVALID) {
            return Collections.emptyList();
        }

        List<AnomalyCandidate> candidates = new ArrayList<>();
        Instant now = context.getCurrentReadingTime();

        // 1. Frost hazard
        if (value.compareTo(FROST_THRESHOLD) <= 0) {
            BigDecimal expected = BigDecimal.valueOf(15.0);
            BigDecimal deviation = value.subtract(expected).setScale(1, RoundingMode.HALF_UP);
            candidates.add(new AnomalyCandidate(
                    AnomalyType.TEMPERATURE_ANOMALY,
                    AnomalySeverity.HIGH,
                    value,
                    expected,
                    deviation,
                    BigDecimal.valueOf(0.92),
                    "Ayoz / Qorasovuq xavfi",
                    String.format("Harorat %.1f°C ga tushib ketdi. Ekin maydonlarida muzlash va ayoz xavfi mavjud.", value.doubleValue()),
                    "RULE_ENGINE",
                    List.of(AnomalyEvidence.of("FROST_WARNING", "TEMPERATURE", value, FROST_THRESHOLD, now, null)),
                    context.isSimulated()
            ));
            return candidates;
        }

        // 2. Extreme heat hazard
        if (value.compareTo(EXTREME_HEAT_THRESHOLD) >= 0) {
            BigDecimal expected = BigDecimal.valueOf(32.0);
            BigDecimal deviation = value.subtract(expected).setScale(1, RoundingMode.HALF_UP);
            candidates.add(new AnomalyCandidate(
                    AnomalyType.TEMPERATURE_ANOMALY,
                    AnomalySeverity.HIGH,
                    value,
                    expected,
                    deviation,
                    BigDecimal.valueOf(0.88),
                    "Favqulodda yuqori harorat (Issiq urishi)",
                    String.format("Harorat %.1f°C ga yetdi. Transpiratsiya va qurgʻoqchilik xavfi oʻta yuqori.", value.doubleValue()),
                    "RULE_ENGINE",
                    List.of(AnomalyEvidence.of("HEAT_WAVE", "TEMPERATURE", value, EXTREME_HEAT_THRESHOLD, now, null)),
                    context.isSimulated()
            ));
        }

        return candidates;
    }
}
