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
 * Statistical anomaly detector implementing Z-score calculation, moving average
 * deviations, and isolated physical outlier detection.
 */
@Component
public class StatisticalAnomalyDetector implements AnomalyDetector {

    public static final double Z_SCORE_ANOMALY_THRESHOLD = 3.0;
    public static final double Z_SCORE_SUSPECT_THRESHOLD = 2.0;

    @Override
    public boolean supports(SensorType sensorType) {
        // Statistical evaluation applies across all physical modalities
        return true;
    }

    @Override
    public List<AnomalyCandidate> detect(DetectionContext context) {
        BigDecimal value = context.getCurrentReadingValue();
        BigDecimal mean = context.getBaselineMean();
        BigDecimal stdDev = context.getBaselineStdDev();

        // Need valid value and statistical baseline
        if (value == null || mean == null || stdDev == null || context.getCurrentReadingQuality() == ReadingQuality.INVALID) {
            return Collections.emptyList();
        }

        // Handle zero standard deviation safely
        if (stdDev.compareTo(BigDecimal.ZERO) == 0 || stdDev.compareTo(BigDecimal.valueOf(0.0001)) < 0) {
            return Collections.emptyList();
        }

        // Need at least 5 recent readings to make statistical inference reliable
        if (context.getRecentReadings().size() < 5) {
            return Collections.emptyList();
        }

        List<AnomalyCandidate> candidates = new ArrayList<>();
        Instant now = context.getCurrentReadingTime();

        // Calculate Z-Score: Z = (X - μ) / σ
        double diff = value.subtract(mean).doubleValue();
        double zScore = Math.abs(diff / stdDev.doubleValue());

        if (zScore >= Z_SCORE_ANOMALY_THRESHOLD) {
            BigDecimal deviation = value.subtract(mean).setScale(2, RoundingMode.HALF_UP);
            BigDecimal confidence = BigDecimal.valueOf(Math.min(0.98, 0.70 + (zScore * 0.05))).setScale(2, RoundingMode.HALF_UP);

            candidates.add(new AnomalyCandidate(
                    AnomalyType.SENSOR_OUTLIER,
                    AnomalySeverity.MEDIUM,
                    value,
                    mean,
                    deviation,
                    confidence,
                    "Statistik chetlashish (Ekstremal autlayer)",
                    String.format("Koʻrsatkich (%.2f) tarixiy oʻrtacha qiymatdan (%.2f) %.1f standart ogʻish (Z-score = %.2f) ga farq qilmoqda. Datchik elektr impulsi yoki kutilmagan oʻzgarish boʻlishi mumkin.",
                            value.doubleValue(), mean.doubleValue(), zScore, zScore),
                    "STATISTICAL",
                    List.of(
                            AnomalyEvidence.of("Z_SCORE", "Z", BigDecimal.valueOf(zScore).setScale(2, RoundingMode.HALF_UP), BigDecimal.valueOf(Z_SCORE_ANOMALY_THRESHOLD), now, "{\"mean\": " + mean + ", \"stdDev\": " + stdDev + "}"),
                            AnomalyEvidence.of("HISTORICAL_MEAN", "MEAN", mean, null, now, null)
                    ),
                    context.isSimulated()
            ));
        }

        return candidates;
    }
}
