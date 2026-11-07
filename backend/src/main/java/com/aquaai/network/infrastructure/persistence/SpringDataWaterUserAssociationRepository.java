package com.aquaai.network.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpringDataWaterUserAssociationRepository extends JpaRepository<WaterUserAssociationJpaEntity, UUID> {
    Optional<WaterUserAssociationJpaEntity> findByCode(String code);
}
