package com.aquaai.sensor.infrastructure.persistence;

import com.aquaai.sensor.domain.model.Sensor;
import com.aquaai.sensor.domain.model.SensorPage;
import com.aquaai.sensor.domain.model.SensorStatus;
import com.aquaai.sensor.domain.repository.SensorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Infrastructure persistence adapter for {@link SensorRepository}.
 */
@Repository
@RequiredArgsConstructor
public class SensorRepositoryImpl implements SensorRepository {

    private final SpringDataSensorDeviceRepository springDataRepository;
    private final SensorDeviceJpaMapper mapper;

    @Override
    @Transactional
    public Sensor save(Sensor sensor) {
        if (sensor == null) {
            return null;
        }

        SensorDeviceJpaEntity entityToSave;
        if (sensor.getId() != null) {
            Optional<SensorDeviceJpaEntity> existing = springDataRepository.findById(sensor.getId());
            if (existing.isPresent()) {
                entityToSave = existing.get();
                mapper.updateEntityFromDomain(sensor, entityToSave);
            } else {
                entityToSave = mapper.toEntity(sensor);
            }
        } else {
            entityToSave = mapper.toEntity(sensor);
        }

        SensorDeviceJpaEntity saved = springDataRepository.saveAndFlush(entityToSave);
        return mapper.toDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Sensor> findById(UUID id) {
        if (id == null) {
            return Optional.empty();
        }
        return springDataRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Sensor> findByDeviceIdentifier(String deviceIdentifier) {
        if (deviceIdentifier == null || deviceIdentifier.isBlank()) {
            return Optional.empty();
        }
        return springDataRepository.findByDeviceIdentifier(deviceIdentifier.trim()).map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Sensor> findByFieldId(UUID fieldId) {
        if (fieldId == null) {
            return Collections.emptyList();
        }
        return springDataRepository.findByFieldId(fieldId).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Sensor> findByFieldIdIn(List<UUID> fieldIds) {
        if (fieldIds == null || fieldIds.isEmpty()) {
            return Collections.emptyList();
        }
        return springDataRepository.findByFieldIdIn(fieldIds).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Sensor> findAllActiveSimulated() {
        return springDataRepository.findByStatusAndProvider(SensorStatus.ACTIVE, "SIMULATED").stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public SensorPage findAll(int page, int size) {
        int boundedPage = Math.max(page, 0);
        int boundedSize = Math.clamp(size, 1, 100);
        PageRequest pageRequest = PageRequest.of(boundedPage, boundedSize, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<SensorDeviceJpaEntity> resultPage = springDataRepository.findAll(pageRequest);

        List<Sensor> content = resultPage.getContent().stream()
                .map(mapper::toDomain)
                .toList();

        return new SensorPage(
                content,
                resultPage.getNumber(),
                resultPage.getSize(),
                resultPage.getTotalElements(),
                resultPage.getTotalPages()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByDeviceIdentifier(String deviceIdentifier) {
        if (deviceIdentifier == null || deviceIdentifier.isBlank()) {
            return false;
        }
        return springDataRepository.existsByDeviceIdentifier(deviceIdentifier.trim());
    }

    @Override
    @Transactional
    public void deleteById(UUID id) {
        if (id != null) {
            springDataRepository.deleteById(id);
            springDataRepository.flush();
        }
    }
}
