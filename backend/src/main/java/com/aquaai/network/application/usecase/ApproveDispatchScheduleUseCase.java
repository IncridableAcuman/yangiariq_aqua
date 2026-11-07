package com.aquaai.network.application.usecase;

import com.aquaai.network.api.dto.ApproveDispatchRequest;
import com.aquaai.network.api.dto.DispatchScheduleResponse;
import com.aquaai.network.application.service.WaterNetworkDtoMapper;
import com.aquaai.network.domain.model.DispatchAssignment;
import com.aquaai.network.domain.model.DispatchSchedule;
import com.aquaai.network.domain.port.NetworkAnomalyPort;
import com.aquaai.network.domain.port.NetworkNotificationPort;
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
public class ApproveDispatchScheduleUseCase {

    private final DispatchScheduleRepository dispatchScheduleRepository;
    private final NetworkAnomalyPort anomalyPort;
    private final NetworkNotificationPort notificationPort;
    private final WaterNetworkDtoMapper dtoMapper;

    @Transactional
    public DispatchScheduleResponse execute(UUID id, ApproveDispatchRequest request, UUID currentUserId) {
        DispatchSchedule schedule = dispatchScheduleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Dispatch schedule", id));

        // Revalidate hydraulic safety interlocks before human approval can take effect
        for (DispatchAssignment assignment : schedule.getAssignments()) {
            UUID fieldId = assignment.fieldId();
            if (anomalyPort.hasSuspectedPipeBurst(fieldId)) {
                schedule.markBlocked("Active pipe burst detected on field: " + assignment.fieldName());
                dispatchScheduleRepository.save(schedule);
                throw new BusinessRuleException("Cannot approve dispatch schedule: hydraulic safety interlock active for " + assignment.fieldName());
            }
            if (anomalyPort.hasCriticalPressureDrop(fieldId)) {
                schedule.markBlocked("Critical pressure drop detected on field: " + assignment.fieldName());
                dispatchScheduleRepository.save(schedule);
                throw new BusinessRuleException("Cannot approve dispatch schedule: critical pressure drop active for " + assignment.fieldName());
            }
        }

        String notes = request != null ? request.notes() : null;
        schedule.approve(currentUserId, notes);
        schedule.schedule(); // Transitions from APPROVED to SCHEDULED

        DispatchSchedule saved = dispatchScheduleRepository.save(schedule);
        log.info("Canal dispatch schedule successfully approved by user={}: id={}, title='{}'",
                currentUserId, saved.getId(), saved.getTitle());

        notificationPort.sendDispatchNotification(
                saved.getId(),
                "Canal Dispatch Schedule Approved",
                "Dispatch schedule '" + saved.getTitle() + "' was approved by authorized operator and is now active for execution.",
                "INFO"
        );

        return dtoMapper.toScheduleResponse(saved);
    }
}
