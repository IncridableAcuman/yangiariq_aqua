package com.aquaai.network.infrastructure.persistence;

import com.aquaai.network.domain.model.ActualDispatchDelivery;
import com.aquaai.network.domain.repository.ActualDispatchDeliveryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class ActualDispatchDeliveryRepositoryImpl implements ActualDispatchDeliveryRepository {

    private final SpringDataActualDispatchDeliveryRepository springDataRepository;
    private final ActualDispatchDeliveryJpaMapper mapper;

    @Override
    public ActualDispatchDelivery save(ActualDispatchDelivery delivery) {
        ActualDispatchDeliveryJpaEntity entity = mapper.toJpaEntity(delivery);
        ActualDispatchDeliveryJpaEntity saved = springDataRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<ActualDispatchDelivery> findById(UUID id) {
        return springDataRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<ActualDispatchDelivery> findByDispatchScheduleId(UUID dispatchScheduleId) {
        return springDataRepository.findByDispatchScheduleId(dispatchScheduleId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<ActualDispatchDelivery> findByFieldId(UUID fieldId) {
        return springDataRepository.findByFieldId(fieldId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<ActualDispatchDelivery> findAll() {
        return springDataRepository.findAll().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public long count() {
        return springDataRepository.count();
    }
}
