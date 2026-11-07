package com.aquaai.network.application.usecase;

import com.aquaai.network.api.dto.DispatchScheduleResponse;
import com.aquaai.network.api.dto.RejectDispatchRequest;
import com.aquaai.network.application.service.WaterNetworkDtoMapper;
import com.aquaai.network.domain.model.DispatchSchedule;
import com.aquaai.network.domain.repository.DispatchScheduleRepository;
import com.aquaai.shared.domain.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RejectDispatchScheduleUseCase {

    private final DispatchScheduleRepository dispatchScheduleRepository;
    private final WaterNetworkDtoMapper dtoMapper;

    @Transactional
    public DispatchScheduleResponse execute(UUID id, RejectDispatchRequest request, UUID currentUserId) {
        DispatchSchedule schedule = dispatchScheduleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Dispatch schedule", id));

        String reason = request != null && request.reason() != null ? request.reason() : "Rejected by operator";
        schedule.reject(currentUserId, reason);

        DispatchSchedule saved = dispatchScheduleRepository.save(schedule);
        log.info("Canal dispatch schedule rejected by user={}: id={}, reason='{}'",
                currentUserId, saved.getId(), reason);

        return dtoMapper.toScheduleResponse(saved);
    }
}
