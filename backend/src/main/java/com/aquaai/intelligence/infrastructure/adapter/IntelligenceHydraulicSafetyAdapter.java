package com.aquaai.intelligence.infrastructure.adapter;

import com.aquaai.anomaly.domain.model.Anomaly;
import com.aquaai.anomaly.domain.model.AnomalyStatus;
import com.aquaai.anomaly.domain.model.AnomalyType;
import com.aquaai.anomaly.domain.repository.AnomalyRepository;
import com.aquaai.intelligence.domain.port.IntelligenceHydraulicSafetyPort;
import com.aquaai.irrigation.domain.service.HydraulicSafetyInterlock;
import com.aquaai.sensor.domain.model.SensorReading;
import com.aquaai.sensor.domain.model.SensorType;
import com.aquaai.sensor.domain.repository.SensorReadingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class IntelligenceHydraulicSafetyAdapter implements IntelligenceHydraulicSafetyPort {

    private final AnomalyRepository anomalyRepository;
    private final SensorReadingRepository sensorReadingRepository;
    private final HydraulicSafetyInterlock interlock = new HydraulicSafetyInterlock();

    @Override
    public HydraulicSafetyStatus checkHydraulicSafety(UUID fieldId) {
        if (fieldId == null) {
            return new HydraulicSafetyStatus(false, null);
        }

        List<Anomaly> active = anomalyRepository.findAll(
                fieldId,
                null,
                AnomalyStatus.DETECTED,
                null,
                null,
                null,
                null,
                null,
                0,
                50
        );

        boolean hasPipeBurst = false;
        boolean hasPressureDrop = false;

        for (Anomaly a : active) {
            if (a.getAnomalyType() == AnomalyType.SUSPECTED_PIPE_BURST) {
                hasPipeBurst = true;
            } else if (a.getAnomalyType() == AnomalyType.WATER_PRESSURE_DROP) {
                hasPressureDrop = true;
            }
        }

        BigDecimal pressureBar = null;
        BigDecimal flowM3H = null;

        List<SensorReading> latest = sensorReadingRepository.findLatestByFieldIdGroupedByType(fieldId);
        for (SensorReading r : latest) {
            if (r.getType() == SensorType.PRESSURE) {
                pressureBar = r.getValue();
            } else if (r.getType() == SensorType.WATER_FLOW) {
                flowM3H = r.getValue();
            }
        }

        HydraulicSafetyInterlock.InterlockResult result = interlock.evaluate(
                hasPipeBurst,
                hasPressureDrop,
                pressureBar,
                flowM3H
        );

        return new HydraulicSafetyStatus(result.isBlocked(), result.blockedReason());
    }
}
