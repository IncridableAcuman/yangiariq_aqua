package com.aquaai.network.application.usecase;

import com.aquaai.network.api.dto.ActualDeliveryResponse;
import com.aquaai.network.application.service.WaterNetworkDtoMapper;
import com.aquaai.network.domain.model.ActualDispatchDelivery;
import com.aquaai.network.domain.repository.ActualDispatchDeliveryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ListActualDeliveriesUseCase {

    private final ActualDispatchDeliveryRepository deliveryRepository;
    private final WaterNetworkDtoMapper dtoMapper;

    @Transactional(readOnly = true)
    public List<ActualDeliveryResponse> execute(UUID dispatchScheduleId, UUID fieldId) {
        List<ActualDispatchDelivery> deliveries;
        if (dispatchScheduleId != null) {
            deliveries = deliveryRepository.findByDispatchScheduleId(dispatchScheduleId);
        } else if (fieldId != null) {
            deliveries = deliveryRepository.findByFieldId(fieldId);
        } else {
            deliveries = deliveryRepository.findAll();
        }

        return deliveries.stream()
                .sorted((a, b) -> b.getActualStartAt().compareTo(a.getActualStartAt()))
                .map(dtoMapper::toDeliveryResponse)
                .collect(Collectors.toList());
    }
}
