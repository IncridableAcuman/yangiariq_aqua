package com.aquaai.network.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpringDataDispatchScheduleRepository extends JpaRepository<DispatchScheduleJpaEntity, UUID> {
    List<DispatchScheduleJpaEntity> findByStatus(String status);
    List<DispatchScheduleJpaEntity> findByTargetWuaId(UUID targetWuaId);

    @Query(value = "SELECT * FROM canal_dispatch_schedules WHERE status IN ('APPROVED', 'SCHEDULED', 'IN_PROGRESS') ORDER BY updated_at DESC LIMIT 1", nativeQuery = true)
    Optional<DispatchScheduleJpaEntity> findLatestActiveSchedule();
}
