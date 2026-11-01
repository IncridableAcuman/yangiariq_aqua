package com.aquaai.anomaly.infrastructure.detection;

import com.aquaai.anomaly.domain.detection.AnomalyDetector;
import com.aquaai.anomaly.domain.detection.DetectionContext;
import com.aquaai.anomaly.domain.model.AnomalyCandidate;
import com.aquaai.anomaly.domain.model.AnomalyEvidence;
import com.aquaai.anomaly.domain.model.AnomalySeverity;
import com.aquaai.anomaly.domain.model.AnomalyType;
import com.aquaai.sensor.domain.model.SensorType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Sensor hardware and data fidelity detector.
 * Detects frozen/stuck telemetry values and gradual possible sensor drift.
 */
@Component
public class SensorHealthDetector implements AnomalyDetector {

    public static final int MIN_STUCK_READINGS = 5;

    @Override
    public boolean supports(SensorType sensorType) {
        return true;
    }

    @Override
    public List<AnomalyCandidate> detect(DetectionContext context) {
        BigDecimal value = context.getCurrentReadingValue();
        if (value == null) {
            return Collections.emptyList();
        }

        List<AnomalyCandidate> candidates = new ArrayList<>();
        Instant now = context.getCurrentReadingTime();
        List<DetectionContext.HistoricalReadingPoint> recent = context.getRecentReadings();

        // 1. Stuck sensor check: values should naturally have slight jitter in nature
        // If last N readings are exactly identical (to within 0.0001) for dynamic types (soil moisture, flow, temperature, pressure)
        if (recent.size() >= MIN_STUCK_READINGS && isNaturallyFluctuating(context.getSensorType())) {
            boolean allEqual = true;
            for (DetectionContext.HistoricalReadingPoint p : recent.subList(0, MIN_STUCK_READINGS)) {
                if (p.value() == null || p.value().subtract(value).abs().compareTo(BigDecimal.valueOf(0.001)) > 0) {
                    allEqual = false;
                    break;
                }
            }

            if (allEqual) {
                candidates.add(new AnomalyCandidate(
                        AnomalyType.SENSOR_STUCK,
                        AnomalySeverity.MEDIUM,
                        value,
                        value,
                        BigDecimal.ZERO,
                        BigDecimal.valueOf(0.85),
                        "Datchik qotib qolgan (Muzlagan qiymat)",
                        String.format("Datchik soʻnggi %d ta ketma-ket oʻlchovda aynan bir xil (%.2f) qiymatni qaytardi. Qurilma qotib qolgan yoki zont ifloslangan boʻlishi mumkin.",
                                MIN_STUCK_READINGS, value.doubleValue()),
                        "SENSOR_HEALTH",
                        List.of(AnomalyEvidence.of("STUCK_READINGS_COUNT", "COUNT", BigDecimal.valueOf(MIN_STUCK_READINGS), null, now, null)),
                        context.isSimulated()
                ));
                return candidates;
            }
        }

        // 2. Possible sensor drift check (Refinement #3: Classified as POSSIBLE_SENSOR_DRIFT)
        // If historical baseline mean exists and recent average has drifted monotonically away by > 25% without external justification
        if (context.getBaselineMean() != null && recent.size() >= 8) {
            BigDecimal recentSum = BigDecimal.ZERO;
            int count = 0;
            for (DetectionContext.HistoricalReadingPoint p : recent) {
                if (p.value() != null) {
                    recentSum = recentSum.add(p.value());
                    count++;
                }
            }
            if (count >= 8) {
                BigDecimal recentAvg = recentSum.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP);
                BigDecimal baseline = context.getBaselineMean();
                BigDecimal delta = recentAvg.subtract(baseline).abs();

                // If delta is substantial (> 30% of baseline and baseline > 0)
                if (baseline.compareTo(BigDecimal.ZERO) > 0) {
                    BigDecimal ratio = delta.divide(baseline, 4, RoundingMode.HALF_UP);
                    if (ratio.compareTo(BigDecimal.valueOf(0.35)) > 0) {
                        candidates.add(new AnomalyCandidate(
                                AnomalyType.POSSIBLE_SENSOR_DRIFT,
                                AnomalySeverity.LOW,
                                recentAvg,
                                baseline,
                                recentAvg.subtract(baseline).setScale(2, RoundingMode.HALF_UP),
                                BigDecimal.valueOf(0.70), // Provisional confidence
                                "Ehtimoliy datchik dreyfi (Kalibrovka ogʻishi)",
                                String.format("Datchik oʻrtacha koʻrsatkichi (%.2f) bazaviy tarixiy meʼyordan (%.2f) asta-sekin uzoqlashmoqda. Laboratoriya kalibrovkasisiz bu faqat ehtimoliy dreyf sifatida baholanadi.",
                                        recentAvg.doubleValue(), baseline.doubleValue()),
                                "SENSOR_HEALTH",
                                List.of(
                                        AnomalyEvidence.of("ROLLING_AVERAGE", "RECENT_AVG", recentAvg, baseline, now, null),
                                        AnomalyEvidence.of("HISTORICAL_BASELINE", "BASELINE_MEAN", baseline, null, now, null)
                                ),
                                context.isSimulated()
                        ));
                    }
                }
            }
        }

        return candidates;
    }

    private boolean isNaturallyFluctuating(SensorType type) {
        return type == SensorType.SOIL_MOISTURE
                || type == SensorType.TEMPERATURE
                || type == SensorType.PRESSURE
                || type == SensorType.WATER_FLOW;
    }
}
