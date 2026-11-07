package com.aquaai.network.infrastructure.persistence;

import com.aquaai.network.domain.model.DispatchSchedule;
import com.aquaai.network.domain.model.DispatchStatus;
import com.aquaai.network.domain.repository.DispatchScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class DispatchScheduleRepositoryImpl implements DispatchScheduleRepository {

    private final SpringDataDispatchScheduleRepository springDataRepository;
    private final DispatchScheduleJpaMapper mapper;

    @Override
    public DispatchSchedule save(DispatchSchedule schedule) {
        DispatchScheduleJpaEntity entity = mapper.toJpaEntity(schedule);
        DispatchScheduleJpaEntity saved = springDataRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<DispatchSchedule> findById(UUID id) {
        return springDataRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<DispatchSchedule> findAll() {
        return springDataRepository.findAll().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<DispatchSchedule> findByStatus(DispatchStatus status) {
        return springDataRepository.findByStatus(status.name()).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<DispatchSchedule> findByTargetWuaId(UUID targetWuaId) {
        return springDataRepository.findByTargetWuaId(targetWuaId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<DispatchSchedule> findLatestActiveSchedule() {
        return springDataRepository.findLatestActiveSchedule().map(mapper::toDomain);
    }

    @Override
    public long count() {
        return springDataRepository.count();
    }
}
