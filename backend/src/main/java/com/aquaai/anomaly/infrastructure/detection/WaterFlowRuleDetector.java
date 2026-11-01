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
 * Deterministic rule detector for irrigation water flow observations.
 * Evaluates unexpected spikes, zero-flow during active irrigation, and unallocated flow delivery.
 */
@Component
public class WaterFlowRuleDetector implements AnomalyDetector {

    public static final BigDecimal DEFAULT_SPIKE_THRESHOLD = BigDecimal.valueOf(45.0); // 45 m³/h
    public static final BigDecimal ZERO_FLOW_THRESHOLD = BigDecimal.valueOf(0.1);      // < 0.1 m³/h

    @Override
    public boolean supports(SensorType sensorType) {
        return sensorType == SensorType.WATER_FLOW;
    }

    @Override
    public List<AnomalyCandidate> detect(DetectionContext context) {
        BigDecimal value = context.getCurrentReadingValue();
        if (value == null || context.getCurrentReadingQuality() == ReadingQuality.INVALID) {
            return Collections.emptyList();
        }

        List<AnomalyCandidate> candidates = new ArrayList<>();
        Instant now = context.getCurrentReadingTime();

        BigDecimal spikeThreshold = context.getRuleConfig() != null && context.getRuleConfig().getThreshold() != null
                ? context.getRuleConfig().getThreshold()
                : DEFAULT_SPIKE_THRESHOLD;

        // 1. Zero flow during active irrigation
        if (context.isHasActiveIrrigationEvent() && value.compareTo(ZERO_FLOW_THRESHOLD) < 0) {
            candidates.add(new AnomalyCandidate(
                    AnomalyType.WATER_FLOW_ZERO,
                    AnomalySeverity.HIGH,
                    value,
                    BigDecimal.valueOf(15.0), // Nominal expected delivery
                    BigDecimal.valueOf(-15.0),
                    BigDecimal.valueOf(0.95),
                    "Sugʻorish vaqtida suv oqimi mavjud emas (Nol oqim)",
                    "Ushbu konturda sugʻorish tadbiri faol holatda boʻlsa-da, suv oqimi qayd etilmadi (0 m³/soat). Kanal qulfi ochilmagan yoki magistral toʻsilgan boʻlishi mumkin.",
                    "RULE_ENGINE",
                    List.of(AnomalyEvidence.of(
                            "IRRIGATION_EVENT_ACTIVE",
                            "WATER_FLOW",
                            value,
                            BigDecimal.ZERO,
                            now,
                            "{\"hasActiveIrrigation\": true}"
                    )),
                    context.isSimulated()
            ));
            return candidates;
        }

        // 2. Flow spike
        if (value.compareTo(spikeThreshold) > 0) {
            BigDecimal expected = context.getBaselineMean() != null ? context.getBaselineMean() : BigDecimal.valueOf(20.0);
            BigDecimal deviation = value.subtract(expected).setScale(2, RoundingMode.HALF_UP);

            candidates.add(new AnomalyCandidate(
                    AnomalyType.WATER_FLOW_SPIKE,
                    AnomalySeverity.HIGH,
                    value,
                    expected,
                    deviation,
                    BigDecimal.valueOf(0.85),
                    "Suv oqimi meʼyordan yuqori (Oqim sakrashi)",
                    String.format("Suv oqimi %.1f m³/soat ga yetib, belgilangan meʼyordan (%.1f m³/soat) oshib ketdi. Quvur bosimi va klapan holatini tekshirish tavsiya etiladi.",
                            value.doubleValue(), spikeThreshold.doubleValue()),
                    "RULE_ENGINE",
                    List.of(AnomalyEvidence.of(
                            "FLOW_SPIKE_BREACH",
                            "WATER_FLOW",
                            value,
                            spikeThreshold,
                            now,
                            "{\"baselineMean\": " + expected + "}"
                    )),
                    context.isSimulated()
            ));
            return candidates;
        }

        // 3. Flow delivery outside active irrigation
        if (!context.isHasActiveIrrigationEvent() && value.compareTo(BigDecimal.valueOf(5.0)) > 0) {
            candidates.add(new AnomalyCandidate(
                    AnomalyType.WATER_FLOW_SPIKE,
                    AnomalySeverity.MEDIUM,
                    value,
                    BigDecimal.ZERO,
                    value,
                    BigDecimal.valueOf(0.78),
                    "Rejadan tashqari suv oqimi",
                    String.format("Ushbu dalada faol sugʻorish rejasi mavjud emas, lekin %.1f m³/soat suv oqimi oʻlchanmoqda. Ruxsatsiz suv olish yoki klapan oqishi ehtimoli bor.",
                            value.doubleValue()),
                    "RULE_ENGINE",
                    List.of(AnomalyEvidence.of(
                            "UNSCHEDULED_FLOW",
                            "WATER_FLOW",
                            value,
                            BigDecimal.ZERO,
                            now,
                            "{\"hasActiveIrrigation\": false}"
                    )),
                    context.isSimulated()
            ));
        }

        return candidates;
    }
}
