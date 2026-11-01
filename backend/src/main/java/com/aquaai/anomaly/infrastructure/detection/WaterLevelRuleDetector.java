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
 * Deterministic rule detector for canal and water basin stage levels.
 */
@Component
public class WaterLevelRuleDetector implements AnomalyDetector {

    public static final BigDecimal DEFAULT_MIN_LEVEL = BigDecimal.valueOf(0.40); // 0.40 m

    @Override
    public boolean supports(SensorType sensorType) {
        return sensorType == SensorType.WATER_LEVEL;
    }

    @Override
    public List<AnomalyCandidate> detect(DetectionContext context) {
        BigDecimal value = context.getCurrentReadingValue();
        if (value == null || context.getCurrentReadingQuality() == ReadingQuality.INVALID) {
            return Collections.emptyList();
        }

        List<AnomalyCandidate> candidates = new ArrayList<>();
        Instant now = context.getCurrentReadingTime();

        BigDecimal minLevel = context.getRuleConfig() != null && context.getRuleConfig().getThreshold() != null
                ? context.getRuleConfig().getThreshold()
                : DEFAULT_MIN_LEVEL;

        if (value.compareTo(minLevel) < 0) {
            BigDecimal expected = BigDecimal.valueOf(1.20);
            BigDecimal deviation = value.subtract(expected).setScale(2, RoundingMode.HALF_UP);

            candidates.add(new AnomalyCandidate(
                    AnomalyType.WATER_LEVEL_DROP,
                    AnomalySeverity.HIGH,
                    value,
                    expected,
                    deviation,
                    BigDecimal.valueOf(0.85),
                    "Kanalda suv sathining kritik pasayishi",
                    String.format("Kanal / havza suvi sathi %.2f m gacha pasayib, xavfsiz minimal sathdan (%.2f m) tushib ketdi. Nasoslar kavitatsiyasi yoki suv yetishmovchiligi xavfi bor.",
                            value.doubleValue(), minLevel.doubleValue()),
                    "RULE_ENGINE",
                    List.of(AnomalyEvidence.of(
                            "CRITICAL_WATER_LEVEL",
                            "WATER_LEVEL",
                            value,
                            minLevel,
                            now,
                            "{\"minLevel\": " + minLevel + "}"
                    )),
                    context.isSimulated()
            ));
        }

        return candidates;
    }
}
