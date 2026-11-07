package com.aquaai.network.application.usecase;

import com.aquaai.network.api.dto.DispatchScheduleResponse;
import com.aquaai.network.application.service.WaterNetworkDtoMapper;
import com.aquaai.network.domain.model.DispatchSchedule;
import com.aquaai.network.domain.repository.DispatchScheduleRepository;
import com.aquaai.shared.domain.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetDispatchScheduleDetailUseCase {

    private final DispatchScheduleRepository dispatchScheduleRepository;
    private final WaterNetworkDtoMapper dtoMapper;

    @Transactional(readOnly = true)
    public DispatchScheduleResponse execute(UUID id) {
        DispatchSchedule schedule = dispatchScheduleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Dispatch schedule", id));
        return dtoMapper.toScheduleResponse(schedule);
    }
}
