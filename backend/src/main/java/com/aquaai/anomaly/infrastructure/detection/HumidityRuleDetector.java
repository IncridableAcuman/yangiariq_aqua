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
public class HumidityRuleDetector implements AnomalyDetector {

    public static final BigDecimal EXTREME_DRY_HUMIDITY = BigDecimal.valueOf(15.0); // < 15%

    @Override
    public boolean supports(SensorType sensorType) {
        return sensorType == SensorType.HUMIDITY;
    }

    @Override
    public List<AnomalyCandidate> detect(DetectionContext context) {
        BigDecimal value = context.getCurrentReadingValue();
        if (value == null || context.getCurrentReadingQuality() == ReadingQuality.INVALID) {
            return Collections.emptyList();
        }

        List<AnomalyCandidate> candidates = new ArrayList<>();
        Instant now = context.getCurrentReadingTime();

        if (value.compareTo(EXTREME_DRY_HUMIDITY) < 0) {
            BigDecimal expected = BigDecimal.valueOf(45.0);
            BigDecimal deviation = value.subtract(expected).setScale(1, RoundingMode.HALF_UP);
            candidates.add(new AnomalyCandidate(
                    AnomalyType.HUMIDITY_ANOMALY,
                    AnomalySeverity.MEDIUM,
                    value,
                    expected,
                    deviation,
                    BigDecimal.valueOf(0.80),
                    "Havo namligining oʻta pastligi (Quruq garmsel)",
                    String.format("Nisbiy namlik %.1f%% gacha pasaydi. Ekin barglaridan jadal bugʻlanish (garmsel taʼsiri) kuzatilishi mumkin.", value.doubleValue()),
                    "RULE_ENGINE",
                    List.of(AnomalyEvidence.of("EXTREME_DRY", "HUMIDITY", value, EXTREME_DRY_HUMIDITY, now, null)),
                    context.isSimulated()
            ));
        }

        return candidates;
    }
}
