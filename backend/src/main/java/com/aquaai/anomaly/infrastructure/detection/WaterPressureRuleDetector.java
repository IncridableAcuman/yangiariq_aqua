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
 * Deterministic rule detector for hydraulic pressure in irrigation networks.
 * Detects sudden pressure drops, over-pressurization, and pipeline instabilities.
 */
@Component
public class WaterPressureRuleDetector implements AnomalyDetector {

    public static final BigDecimal DEFAULT_LOW_PRESSURE_THRESHOLD = BigDecimal.valueOf(1.2);  // < 1.2 bar
    public static final BigDecimal DEFAULT_HIGH_PRESSURE_THRESHOLD = BigDecimal.valueOf(5.5); // > 5.5 bar

    @Override
    public boolean supports(SensorType sensorType) {
        return sensorType == SensorType.PRESSURE;
    }

    @Override
    public List<AnomalyCandidate> detect(DetectionContext context) {
        BigDecimal value = context.getCurrentReadingValue();
        if (value == null || context.getCurrentReadingQuality() == ReadingQuality.INVALID) {
            return Collections.emptyList();
        }

        List<AnomalyCandidate> candidates = new ArrayList<>();
        Instant now = context.getCurrentReadingTime();

        BigDecimal lowThreshold = context.getRuleConfig() != null && context.getRuleConfig().getThreshold() != null
                ? context.getRuleConfig().getThreshold()
                : DEFAULT_LOW_PRESSURE_THRESHOLD;

        BigDecimal highThreshold = context.getRuleConfig() != null && context.getRuleConfig().getSecondaryThreshold() != null
                ? context.getRuleConfig().getSecondaryThreshold()
                : DEFAULT_HIGH_PRESSURE_THRESHOLD;

        // 1. Sudden pressure drop (possible leak or burst upstream)
        if (value.compareTo(lowThreshold) < 0 && context.isHasActiveIrrigationEvent()) {
            BigDecimal expected = BigDecimal.valueOf(2.5); // Nominal working pressure
            BigDecimal deviation = value.subtract(expected).setScale(2, RoundingMode.HALF_UP);

            candidates.add(new AnomalyCandidate(
                    AnomalyType.WATER_PRESSURE_DROP,
                    AnomalySeverity.HIGH,
                    value,
                    expected,
                    deviation,
                    BigDecimal.valueOf(0.88),
                    "Gidravlik bosimning keskin pasayishi",
                    String.format("Sugʻorish tarmogʻidagi bosim %.2f bar gacha pasaydi (meʼyor: %.1f bar). Quvur yorilishi, qochqin yoki nasos stansiyasi bosim pasayishi ehtimoli yuqori.",
                            value.doubleValue(), expected.doubleValue()),
                    "RULE_ENGINE",
                    List.of(AnomalyEvidence.of(
                            "PRESSURE_DROP",
                            "PRESSURE",
                            value,
                            expected,
                            now,
                            "{\"lowThreshold\": " + lowThreshold + "}"
                    )),
                    context.isSimulated()
            ));
            return candidates;
        }

        // 2. High pressure hazard
        if (value.compareTo(highThreshold) > 0) {
            BigDecimal deviation = value.subtract(highThreshold).setScale(2, RoundingMode.HALF_UP);
            candidates.add(new AnomalyCandidate(
                    AnomalyType.WATER_PRESSURE_DROP,
                    AnomalySeverity.HIGH,
                    value,
                    highThreshold,
                    deviation,
                    BigDecimal.valueOf(0.85),
                    "Gidravlik yuqori bosim xavfi",
                    String.format("Tizimdagi bosim %.2f bar ga koʻtarilib, maksimal xavfsiz meʼyordan (%.1f bar) oshdi. Quvurlar shikastlanishi va ulanishlar uzilishi xavfi mavjud.",
                            value.doubleValue(), highThreshold.doubleValue()),
                    "RULE_ENGINE",
                    List.of(AnomalyEvidence.of(
                            "OVER_PRESSURE",
                            "PRESSURE",
                            value,
                            highThreshold,
                            now,
                            "{\"highThreshold\": " + highThreshold + "}"
                    )),
                    context.isSimulated()
            ));
        }

        return candidates;
    }
}
