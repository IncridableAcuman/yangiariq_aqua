package com.aquaai.network.application.usecase;

import com.aquaai.network.api.dto.DispatchScheduleResponse;
import com.aquaai.network.application.service.WaterNetworkDtoMapper;
import com.aquaai.network.domain.model.DispatchSchedule;
import com.aquaai.network.domain.model.DispatchStatus;
import com.aquaai.network.domain.repository.DispatchScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ListDispatchSchedulesUseCase {

    private final DispatchScheduleRepository dispatchScheduleRepository;
    private final WaterNetworkDtoMapper dtoMapper;

    @Transactional(readOnly = true)
    public List<DispatchScheduleResponse> execute(String status, UUID targetWuaId) {
        List<DispatchSchedule> schedules;
        if (status != null && !status.isBlank()) {
            try {
                DispatchStatus ds = DispatchStatus.valueOf(status.toUpperCase());
                schedules = dispatchScheduleRepository.findByStatus(ds);
            } catch (Exception e) {
                schedules = dispatchScheduleRepository.findAll();
            }
        } else if (targetWuaId != null) {
            schedules = dispatchScheduleRepository.findByTargetWuaId(targetWuaId);
        } else {
            schedules = dispatchScheduleRepository.findAll();
        }

        return schedules.stream()
                .sorted((a, b) -> b.getUpdatedAt().compareTo(a.getUpdatedAt()))
                .map(dtoMapper::toScheduleResponse)
                .collect(Collectors.toList());
    }
}
