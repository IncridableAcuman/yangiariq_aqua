package com.aquaai.network.infrastructure.persistence;

import com.aquaai.network.domain.model.ActualDispatchDelivery;
import org.springframework.stereotype.Component;

@Component
public class ActualDispatchDeliveryJpaMapper {

    public ActualDispatchDeliveryJpaEntity toJpaEntity(ActualDispatchDelivery domain) {
        if (domain == null) return null;
        return ActualDispatchDeliveryJpaEntity.builder()
                .id(domain.getId())
                .dispatchScheduleId(domain.getDispatchScheduleId())
                .fieldId(domain.getFieldId())
                .fieldName(domain.getFieldName())
                .edgeId(domain.getEdgeId())
                .canalName(domain.getCanalName())
                .actualStartAt(domain.getActualStartAt())
                .actualEndAt(domain.getActualEndAt())
                .plannedVolumeM3(domain.getPlannedVolumeM3())
                .actualVolumeM3(domain.getActualVolumeM3())
                .varianceVolumeM3(domain.getVarianceVolumeM3())
                .variancePercent(domain.getVariancePercent())
                .measuredFlowM3S(domain.getMeasuredFlowM3S())
                .operatorId(domain.getOperatorId())
                .operatorNotes(domain.getOperatorNotes())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();
    }

    public ActualDispatchDelivery toDomain(ActualDispatchDeliveryJpaEntity entity) {
        if (entity == null) return null;
        return ActualDispatchDelivery.builder()
                .id(entity.getId())
                .dispatchScheduleId(entity.getDispatchScheduleId())
                .fieldId(entity.getFieldId())
                .fieldName(entity.getFieldName())
                .edgeId(entity.getEdgeId())
                .canalName(entity.getCanalName())
                .actualStartAt(entity.getActualStartAt())
                .actualEndAt(entity.getActualEndAt())
                .plannedVolumeM3(entity.getPlannedVolumeM3())
                .actualVolumeM3(entity.getActualVolumeM3())
                .varianceVolumeM3(entity.getVarianceVolumeM3())
                .variancePercent(entity.getVariancePercent())
                .measuredFlowM3S(entity.getMeasuredFlowM3S())
                .operatorId(entity.getOperatorId())
                .operatorNotes(entity.getOperatorNotes())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
