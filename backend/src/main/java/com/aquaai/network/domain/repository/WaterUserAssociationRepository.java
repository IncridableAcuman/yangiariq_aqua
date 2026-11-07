package com.aquaai.network.domain.repository;

import com.aquaai.network.domain.model.WaterUserAssociation;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WaterUserAssociationRepository {
    WaterUserAssociation save(WaterUserAssociation wua);
    Optional<WaterUserAssociation> findById(UUID id);
    Optional<WaterUserAssociation> findByCode(String code);
    List<WaterUserAssociation> findAll();
    long count();
}
