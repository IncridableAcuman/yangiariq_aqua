package com.aquaai.network.application.usecase;

import com.aquaai.network.api.dto.DispatchScheduleResponse;
import com.aquaai.network.application.service.WaterNetworkDtoMapper;
import com.aquaai.network.domain.model.DispatchSchedule;
import com.aquaai.network.domain.model.DispatchStatus;
import com.aquaai.network.domain.repository.DispatchScheduleRepository;
import com.aquaai.shared.domain.BusinessRuleException;
import com.aquaai.shared.domain.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExecuteDispatchScheduleUseCase {

    private final DispatchScheduleRepository dispatchScheduleRepository;
    private final WaterNetworkDtoMapper dtoMapper;

    @Transactional
    public DispatchScheduleResponse startExecution(UUID id, UUID currentUserId) {
        DispatchSchedule schedule = dispatchScheduleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Dispatch schedule", id));

        if (schedule.getStatus() != DispatchStatus.SCHEDULED && schedule.getStatus() != DispatchStatus.APPROVED) {
            throw new BusinessRuleException("Only APPROVED or SCHEDULED dispatches can start execution. Current status: " + schedule.getStatus());
        }

        schedule.startExecution();
        DispatchSchedule saved = dispatchScheduleRepository.save(schedule);
        log.info("Canal dispatch execution started by user={}: id={}, title='{}'", currentUserId, saved.getId(), saved.getTitle());
        return dtoMapper.toScheduleResponse(saved);
    }

    @Transactional
    public DispatchScheduleResponse completeExecution(UUID id, UUID currentUserId) {
        DispatchSchedule schedule = dispatchScheduleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Dispatch schedule", id));

        if (schedule.getStatus() != DispatchStatus.IN_PROGRESS) {
            throw new BusinessRuleException("Only IN_PROGRESS dispatches can be marked completed. Current status: " + schedule.getStatus());
        }

        schedule.completeExecution();
        DispatchSchedule saved = dispatchScheduleRepository.save(schedule);
        log.info("Canal dispatch execution completed by user={}: id={}, title='{}'", currentUserId, saved.getId(), saved.getTitle());
        return dtoMapper.toScheduleResponse(saved);
    }
}
