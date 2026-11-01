package com.aquaai.sensor.domain.repository;

import com.aquaai.sensor.domain.model.Sensor;
import com.aquaai.sensor.domain.model.SensorPage;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Domain repository port for {@link Sensor} aggregate roots.
 */
public interface SensorRepository {

    Sensor save(Sensor sensor);

    Optional<Sensor> findById(UUID id);

    Optional<Sensor> findByDeviceIdentifier(String deviceIdentifier);

    List<Sensor> findByFieldId(UUID fieldId);

    List<Sensor> findByFieldIdIn(List<UUID> fieldIds);

    List<Sensor> findAllActiveSimulated();

    SensorPage findAll(int page, int size);

    boolean existsByDeviceIdentifier(String deviceIdentifier);

    void deleteById(UUID id);
}
