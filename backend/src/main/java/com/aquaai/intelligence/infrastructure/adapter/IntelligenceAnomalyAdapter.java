package com.aquaai.intelligence.infrastructure.adapter;

import com.aquaai.anomaly.domain.model.Anomaly;
import com.aquaai.anomaly.domain.model.AnomalyStatus;
import com.aquaai.anomaly.domain.model.AnomalyType;
import com.aquaai.anomaly.domain.repository.AnomalyRepository;
import com.aquaai.intelligence.domain.port.IntelligenceAnomalyPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class IntelligenceAnomalyAdapter implements IntelligenceAnomalyPort {

    private final AnomalyRepository anomalyRepository;

    @Override
    public ActiveAnomalyStatus getActiveAnomalyStatus(UUID fieldId) {
        if (fieldId == null) {
            return new ActiveAnomalyStatus(false, false, 0);
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

        return new ActiveAnomalyStatus(hasPipeBurst, hasPressureDrop, active.size());
    }
}
