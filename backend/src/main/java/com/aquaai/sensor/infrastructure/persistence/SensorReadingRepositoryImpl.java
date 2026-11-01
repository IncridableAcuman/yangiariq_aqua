package com.aquaai.sensor.infrastructure.persistence;

import com.aquaai.sensor.domain.model.SensorReading;
import com.aquaai.sensor.domain.model.SensorReadingPage;
import com.aquaai.sensor.domain.repository.SensorReadingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Infrastructure persistence adapter for {@link SensorReadingRepository}.
 */
@Repository
@RequiredArgsConstructor
public class SensorReadingRepositoryImpl implements SensorReadingRepository {

    private static final int MAX_PAGE_SIZE = 100;
    private static final Instant DEFAULT_MIN_INSTANT = Instant.parse("2020-01-01T00:00:00Z");

    private final SpringDataSensorReadingRepository springDataRepository;
    private final SensorReadingJpaMapper mapper;

    @Override
    @Transactional
    public SensorReading save(SensorReading reading) {
        if (reading == null) {
            return null;
        }
        SensorReadingJpaEntity entity = mapper.toEntity(reading);
        SensorReadingJpaEntity saved = springDataRepository.saveAndFlush(entity);
        return mapper.toDomain(saved);
    }

    @Override
    @Transactional
    public List<SensorReading> saveAll(List<SensorReading> readings) {
        if (readings == null || readings.isEmpty()) {
            return Collections.emptyList();
        }
        List<SensorReadingJpaEntity> entities = readings.stream()
                .map(mapper::toEntity)
                .toList();
        List<SensorReadingJpaEntity> saved = springDataRepository.saveAllAndFlush(entities);
        return saved.stream().map(mapper::toDomain).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<SensorReading> findLatestBySensorId(UUID sensorId) {
        if (sensorId == null) {
            return Optional.empty();
        }
        return springDataRepository.findFirstBySensorIdOrderByMeasuredAtDesc(sensorId)
                .map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public SensorReadingPage findBySensorId(
            UUID sensorId,
            Instant from,
            Instant to,
            int page,
            int size,
            String sortDirection
    ) {
        if (sensorId == null) {
            return new SensorReadingPage(Collections.emptyList(), 0, size, 0, 0);
        }

        Instant effectiveFrom = from != null ? from : DEFAULT_MIN_INSTANT;
        Instant effectiveTo = to != null ? to : Instant.now().plus(1, ChronoUnit.DAYS);

        int boundedPage = Math.max(page, 0);
        int boundedSize = Math.clamp(size, 1, MAX_PAGE_SIZE);

        Sort.Direction direction = "asc".equalsIgnoreCase(sortDirection) ? Sort.Direction.ASC : Sort.Direction.DESC;
        PageRequest pageRequest = PageRequest.of(boundedPage, boundedSize, Sort.by(direction, "measuredAt"));

        Page<SensorReadingJpaEntity> resultPage = springDataRepository.findBySensorIdAndMeasuredAtBetween(
                sensorId,
                effectiveFrom,
                effectiveTo,
                pageRequest
        );

        List<SensorReading> content = resultPage.getContent().stream()
                .map(mapper::toDomain)
                .toList();

        return new SensorReadingPage(
                content,
                resultPage.getNumber(),
                resultPage.getSize(),
                resultPage.getTotalElements(),
                resultPage.getTotalPages()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public SensorReadingPage findByFieldId(
            UUID fieldId,
            Instant from,
            Instant to,
            int page,
            int size,
            String sortDirection
    ) {
        if (fieldId == null) {
            return new SensorReadingPage(Collections.emptyList(), 0, size, 0, 0);
        }

        Instant effectiveFrom = from != null ? from : DEFAULT_MIN_INSTANT;
        Instant effectiveTo = to != null ? to : Instant.now().plus(1, ChronoUnit.DAYS);

        int boundedPage = Math.max(page, 0);
        int boundedSize = Math.clamp(size, 1, MAX_PAGE_SIZE);

        Sort.Direction direction = "asc".equalsIgnoreCase(sortDirection) ? Sort.Direction.ASC : Sort.Direction.DESC;
        PageRequest pageRequest = PageRequest.of(boundedPage, boundedSize, Sort.by(direction, "measuredAt"));

        Page<SensorReadingJpaEntity> resultPage = springDataRepository.findByFieldIdAndMeasuredAtBetween(
                fieldId,
                effectiveFrom,
                effectiveTo,
                pageRequest
        );

        List<SensorReading> content = resultPage.getContent().stream()
                .map(mapper::toDomain)
                .toList();

        return new SensorReadingPage(
                content,
                resultPage.getNumber(),
                resultPage.getSize(),
                resultPage.getTotalElements(),
                resultPage.getTotalPages()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<SensorReading> findLatestByFieldIdGroupedByType(UUID fieldId) {
        if (fieldId == null) {
            return Collections.emptyList();
        }
        return springDataRepository.findLatestByFieldIdGroupedByType(fieldId).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<SensorReading> findLatestSoilMoistureByFieldIdIn(java.util.Collection<UUID> fieldIds) {
        if (fieldIds == null || fieldIds.isEmpty()) {
            return Collections.emptyList();
        }
        return springDataRepository.findLatestSoilMoistureByFieldIdIn(fieldIds).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    @Transactional
    public int deleteByMeasuredAtBefore(Instant cutoff) {
        if (cutoff == null) {
            return 0;
        }
        return springDataRepository.deleteByMeasuredAtBefore(cutoff);
    }
}
