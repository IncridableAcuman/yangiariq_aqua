package com.aquaai.pilot.infrastructure.persistence.repository;

import com.aquaai.pilot.infrastructure.persistence.entity.GroundTruthValidationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SpringDataGroundTruthValidationRepository extends JpaRepository<GroundTruthValidationEntity, UUID> {
    List<GroundTruthValidationEntity> findByPilotIdOrderByMeasuredAtDesc(UUID pilotId);
    List<GroundTruthValidationEntity> findByFieldIdOrderByMeasuredAtDesc(UUID fieldId);
    List<GroundTruthValidationEntity> findByPilotIdAndVerificationStatusOrderByMeasuredAtDesc(UUID pilotId, String verificationStatus);
    List<GroundTruthValidationEntity> findByFieldIdAndMeasurementTypeOrderByMeasuredAtDesc(UUID fieldId, String measurementType);
    long countByPilotId(UUID pilotId);
    long countByPilotIdAndVerificationStatus(UUID pilotId, String verificationStatus);
}
