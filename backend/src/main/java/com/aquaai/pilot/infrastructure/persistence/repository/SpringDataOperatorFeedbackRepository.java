package com.aquaai.pilot.infrastructure.persistence.repository;

import com.aquaai.pilot.infrastructure.persistence.entity.OperatorFeedbackEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SpringDataOperatorFeedbackRepository extends JpaRepository<OperatorFeedbackEntity, UUID> {
    List<OperatorFeedbackEntity> findByPilotIdOrderByCreatedAtDesc(UUID pilotId);
    List<OperatorFeedbackEntity> findByPilotIdAndStatusOrderByCreatedAtDesc(UUID pilotId, String status);
    List<OperatorFeedbackEntity> findByPilotIdAndCategoryOrderByCreatedAtDesc(UUID pilotId, String category);
    long countByPilotId(UUID pilotId);
    long countByPilotIdAndStatus(UUID pilotId, String status);
}
