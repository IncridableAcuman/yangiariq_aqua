package com.aquaai.network.application.usecase;

import com.aquaai.network.api.dto.ActualDeliveryResponse;
import com.aquaai.network.api.dto.RecordActualDeliveryRequest;
import com.aquaai.network.application.service.WaterNetworkDtoMapper;
import com.aquaai.network.domain.model.ActualDispatchDelivery;
import com.aquaai.network.domain.model.WaterNetworkEdge;
import com.aquaai.network.domain.port.NetworkFieldPort;
import com.aquaai.network.domain.repository.ActualDispatchDeliveryRepository;
import com.aquaai.network.domain.repository.WaterNetworkEdgeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecordActualDeliveryUseCase {

    private final ActualDispatchDeliveryRepository deliveryRepository;
    private final NetworkFieldPort fieldPort;
    private final WaterNetworkEdgeRepository edgeRepository;
    private final WaterNetworkDtoMapper dtoMapper;

    @Transactional
    public ActualDeliveryResponse execute(RecordActualDeliveryRequest request, UUID currentUserId) {
        String fieldName = request.fieldName();
        if (fieldName == null || fieldName.isBlank()) {
            fieldName = fieldPort.getFieldInfo(request.fieldId())
                    .map(NetworkFieldPort.FieldParcelInfo::name)
                    .orElse("Field " + request.fieldId());
        }

        String canalName = request.canalName();
        if ((canalName == null || canalName.isBlank()) && request.edgeId() != null) {
            canalName = edgeRepository.findById(request.edgeId())
                    .map(WaterNetworkEdge::getName)
                    .orElse("Canal " + request.edgeId());
        }

        BigDecimal plannedVol = request.plannedVolumeM3() != null ? request.plannedVolumeM3() : BigDecimal.ZERO;
        BigDecimal actualVol = request.actualVolumeM3() != null ? request.actualVolumeM3() : BigDecimal.ZERO;

        ActualDispatchDelivery delivery = ActualDispatchDelivery.builder()
                .dispatchScheduleId(request.dispatchScheduleId())
                .fieldId(request.fieldId())
                .fieldName(fieldName)
                .edgeId(request.edgeId())
                .canalName(canalName != null ? canalName : "Direct Intake")
                .actualStartAt(request.actualStartAt())
                .actualEndAt(request.actualEndAt())
                .plannedVolumeM3(plannedVol)
                .actualVolumeM3(actualVol)
                .measuredFlowM3S(request.measuredFlowM3S() != null ? request.measuredFlowM3S() : BigDecimal.ZERO)
                .operatorId(currentUserId)
                .operatorNotes(request.operatorNotes())
                .build();

        ActualDispatchDelivery saved = deliveryRepository.save(delivery);
        log.info("Recorded actual canal dispatch delivery: id={}, fieldId={}, plannedM3={}, actualM3={}, varianceM3={}",
                saved.getId(), saved.getFieldId(), saved.getPlannedVolumeM3(), saved.getActualVolumeM3(), saved.getVarianceVolumeM3());

        return dtoMapper.toDeliveryResponse(saved);
    }
}
