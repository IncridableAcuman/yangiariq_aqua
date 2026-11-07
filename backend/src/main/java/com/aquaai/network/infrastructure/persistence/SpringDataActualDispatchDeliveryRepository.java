package com.aquaai.network.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SpringDataActualDispatchDeliveryRepository extends JpaRepository<ActualDispatchDeliveryJpaEntity, UUID> {
    List<ActualDispatchDeliveryJpaEntity> findByDispatchScheduleId(UUID dispatchScheduleId);
    List<ActualDispatchDeliveryJpaEntity> findByFieldId(UUID fieldId);
}
