package com.aquaai.network.domain.repository;

import com.aquaai.network.domain.model.ActualDispatchDelivery;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ActualDispatchDeliveryRepository {
    ActualDispatchDelivery save(ActualDispatchDelivery delivery);
    Optional<ActualDispatchDelivery> findById(UUID id);
    List<ActualDispatchDelivery> findByDispatchScheduleId(UUID dispatchScheduleId);
    List<ActualDispatchDelivery> findByFieldId(UUID fieldId);
    List<ActualDispatchDelivery> findAll();
    long count();
}
