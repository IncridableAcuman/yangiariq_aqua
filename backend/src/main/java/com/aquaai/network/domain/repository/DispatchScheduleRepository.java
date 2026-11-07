package com.aquaai.network.domain.repository;

import com.aquaai.network.domain.model.DispatchSchedule;
import com.aquaai.network.domain.model.DispatchStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DispatchScheduleRepository {
    DispatchSchedule save(DispatchSchedule schedule);
    Optional<DispatchSchedule> findById(UUID id);
    List<DispatchSchedule> findAll();
    List<DispatchSchedule> findByStatus(DispatchStatus status);
    List<DispatchSchedule> findByTargetWuaId(UUID targetWuaId);
    Optional<DispatchSchedule> findLatestActiveSchedule();
    long count();
}
