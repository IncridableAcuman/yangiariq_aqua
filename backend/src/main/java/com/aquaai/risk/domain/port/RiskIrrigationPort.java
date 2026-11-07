package com.aquaai.risk.domain.port;

import com.aquaai.irrigation.domain.model.SmartIrrigationPlan;

import java.util.List;
import java.util.UUID;

public interface RiskIrrigationPort {

    List<SmartIrrigationPlan> getActivePlans(UUID fieldId);

    boolean hasScheduleConflict(UUID fieldId);
}
