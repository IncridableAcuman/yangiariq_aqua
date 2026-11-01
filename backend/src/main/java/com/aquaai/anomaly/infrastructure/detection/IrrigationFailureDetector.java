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
 * Cross-metric irrigation failure and hydraulic risk detector.
 * Corroborates active irrigation events with water flow, hydraulic pressure, and soil moisture response.
 * Follows multi-metric pipe burst criteria (Refinement #2).
 */
@Component
public class IrrigationFailureDetector implements AnomalyDetector {

    public static final BigDecimal PIPE_BURST_FLOW_THRESHOLD = BigDecimal.valueOf(50.0); // 50 m³/h
    public static final BigDecimal LOW_PRESSURE_CORROBORATION = BigDecimal.valueOf(1.0);  // < 1.0 bar

    @Override
    public boolean supports(SensorType sensorType) {
        return sensorType == SensorType.WATER_FLOW || sensorType == SensorType.PRESSURE;
    }

    @Override
    public List<AnomalyCandidate> detect(DetectionContext context) {
        BigDecimal value = context.getCurrentReadingValue();
        if (value == null || context.getCurrentReadingQuality() == ReadingQuality.INVALID) {
            return Collections.emptyList();
        }

        List<AnomalyCandidate> candidates = new ArrayList<>();
        Instant now = context.getCurrentReadingTime();

        // 1. Water Flow during Active Irrigation: Evaluate Multi-Metric Pipe Burst
        if (context.getSensorType() == SensorType.WATER_FLOW) {
            if (value.compareTo(PIPE_BURST_FLOW_THRESHOLD) > 0) {
                // Check if simultaneous pressure drop corroborates pipe burst
                BigDecimal pressure = context.getLinePressureBar();
                boolean pressureCorroborated = pressure != null && pressure.compareTo(LOW_PRESSURE_CORROBORATION) < 0;

                if (pressureCorroborated) {
                    // Confirmed multi-metric correlation: flow spike + severe pressure drop
                    candidates.add(new AnomalyCandidate(
                            AnomalyType.SUSPECTED_PIPE_BURST,
                            AnomalySeverity.CRITICAL,
                            value,
                            BigDecimal.valueOf(25.0),
                            value.subtract(BigDecimal.valueOf(25.0)),
                            BigDecimal.valueOf(0.92), // High confidence with multi-metric evidence
                            "Shubhali quvur yorilishi (Oqim yuqori + Bosim keskin pasaygan)",
                            String.format("Suv oqimi keskin oshib (%.1f m³/soat) bir paytda quvur bosimi %.2f bar gacha tushib ketgan. Bu magistral quvur yorilishi yoki fiting ajralishining kuchli belgisidir.",
                                    value.doubleValue(), pressure.doubleValue()),
                            "IRRIGATION_MONITOR",
                            List.of(
                                    AnomalyEvidence.of("FLOW_RATE", "WATER_FLOW", value, BigDecimal.valueOf(25.0), now, null),
                                    AnomalyEvidence.of("PRESSURE_DROP", "PRESSURE", pressure, LOW_PRESSURE_CORROBORATION, now, null)
                            ),
                            context.isSimulated()
                    ));
                } else {
                    // Refinement #2: High flow without pressure corroboration is SUSPECTED with lower confidence
                    BigDecimal confidence = pressure == null ? BigDecimal.valueOf(0.65) : BigDecimal.valueOf(0.55);
                    String explanation = pressure == null
                            ? String.format("Suv oqimi meʼyordan yuqori (%.1f m³/soat), biroq bosim datchigi maʼlumotlari mavjud emas. Voqea ehtimoliy quvur nosozligi sifatida baholandi.", value.doubleValue())
                            : String.format("Suv oqimi yuqori (%.1f m³/soat), biroq tizim bosimi (%.2f bar) normal darajada saqlangan. Toʻliq quvur yorilishi tasdiqlanmadi.", value.doubleValue(), pressure.doubleValue());

                    candidates.add(new AnomalyCandidate(
                            AnomalyType.SUSPECTED_PIPE_BURST,
                            AnomalySeverity.HIGH,
                            value,
                            BigDecimal.valueOf(25.0),
                            value.subtract(BigDecimal.valueOf(25.0)).setScale(2, RoundingMode.HALF_UP),
                            confidence,
                            "Ehtimoliy quvur nosozligi / Oqish",
                            explanation,
                            "IRRIGATION_MONITOR",
                            List.of(AnomalyEvidence.of("HIGH_FLOW_UNVERIFIED", "WATER_FLOW", value, BigDecimal.valueOf(25.0), now, "{\"pressureAvailable\": " + (pressure != null) + "}")),
                            context.isSimulated()
                    ));
                }
            }
        }

        // 2. Irrigation failure: Active irrigation event with zero water flow
        if (context.getSensorType() == SensorType.WATER_FLOW && context.isHasActiveIrrigationEvent()) {
            if (value.compareTo(BigDecimal.valueOf(0.1)) < 0) {
                candidates.add(new AnomalyCandidate(
                        AnomalyType.IRRIGATION_FAILURE,
                        AnomalySeverity.HIGH,
                        value,
                        BigDecimal.valueOf(20.0),
                        BigDecimal.valueOf(-20.0),
                        BigDecimal.valueOf(0.95),
                        "Sugʻorish tizimi nosozligi (Suv yetkazib berilmadi)",
                        "Faol sugʻorish rejasi davom etayotganiga qaramay, suv oqimi 0 m³/soat ni tashkil qilmoqda. Nasos toʻxtab qolgan yoki taqsimlash klapani ochilmagan.",
                        "IRRIGATION_MONITOR",
                        List.of(AnomalyEvidence.of("ZERO_FLOW_DURING_IRRIGATION", "WATER_FLOW", value, BigDecimal.valueOf(20.0), now, null)),
                        context.isSimulated()
                ));
            }
        }

        return candidates;
    }
}
