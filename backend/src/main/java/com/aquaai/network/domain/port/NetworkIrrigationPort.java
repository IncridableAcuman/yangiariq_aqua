package com.aquaai.network.domain.port;

import com.aquaai.irrigation.domain.model.SmartIrrigationPlan;

import java.util.List;
import java.util.UUID;

public interface NetworkIrrigationPort {
    List<SmartIrrigationPlan> getUpcomingPlansForField(UUID fieldId);
    List<SmartIrrigationPlan> getAllActivePlans();
    boolean hasScheduleConflict(UUID fieldId);
}
