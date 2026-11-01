package com.aquaai.sensor.infrastructure.persistence;

import com.aquaai.sensor.domain.model.SensorStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpringDataSensorDeviceRepository extends JpaRepository<SensorDeviceJpaEntity, UUID> {

    Optional<SensorDeviceJpaEntity> findByDeviceIdentifier(String deviceIdentifier);

    List<SensorDeviceJpaEntity> findByFieldId(UUID fieldId);

    List<SensorDeviceJpaEntity> findByFieldIdIn(List<UUID> fieldIds);

    List<SensorDeviceJpaEntity> findByStatusAndProvider(SensorStatus status, String provider);

    boolean existsByDeviceIdentifier(String deviceIdentifier);

    Page<SensorDeviceJpaEntity> findAll(Pageable pageable);
}
