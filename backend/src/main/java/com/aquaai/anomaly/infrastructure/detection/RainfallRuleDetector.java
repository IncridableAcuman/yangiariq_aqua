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

@Component
public class RainfallRuleDetector implements AnomalyDetector {

    public static final BigDecimal EXTREME_RAINFALL_HOURLY = BigDecimal.valueOf(50.0); // 50 mm/h

    @Override
    public boolean supports(SensorType sensorType) {
        return sensorType == SensorType.RAINFALL;
    }

    @Override
    public List<AnomalyCandidate> detect(DetectionContext context) {
        BigDecimal value = context.getCurrentReadingValue();
        if (value == null || context.getCurrentReadingQuality() == ReadingQuality.INVALID) {
            return Collections.emptyList();
        }

        List<AnomalyCandidate> candidates = new ArrayList<>();
        Instant now = context.getCurrentReadingTime();

        if (value.compareTo(EXTREME_RAINFALL_HOURLY) > 0) {
            BigDecimal expected = BigDecimal.valueOf(5.0);
            BigDecimal deviation = value.subtract(expected).setScale(1, RoundingMode.HALF_UP);
            candidates.add(new AnomalyCandidate(
                    AnomalyType.RAINFALL_ANOMALY,
                    AnomalySeverity.HIGH,
                    value,
                    expected,
                    deviation,
                    BigDecimal.valueOf(0.85),
                    "Jala / Kuchli yogʻingarchilik",
                    String.format("Yogʻingarchilik jadalligi %.1f mm/soat ga yetdi. Dala parcellarida suv toshishi va tuproq yuvilishi xavfi mavjud.", value.doubleValue()),
                    "RULE_ENGINE",
                    List.of(AnomalyEvidence.of("EXTREME_RAINFALL", "RAINFALL", value, EXTREME_RAINFALL_HOURLY, now, null)),
                    context.isSimulated()
            ));
        }

        return candidates;
    }
}
