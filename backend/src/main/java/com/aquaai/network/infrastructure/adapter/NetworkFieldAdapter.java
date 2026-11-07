package com.aquaai.network.infrastructure.adapter;

import com.aquaai.field.domain.model.Field;
import com.aquaai.field.domain.repository.FieldRepository;
import com.aquaai.network.domain.port.NetworkFieldPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class NetworkFieldAdapter implements NetworkFieldPort {

    private final FieldRepository fieldRepository;

    @Override
    public Optional<FieldParcelInfo> getFieldInfo(UUID fieldId) {
        if (fieldId == null) return Optional.empty();
        return fieldRepository.findById(fieldId).map(this::mapToInfo);
    }

    @Override
    public List<FieldParcelInfo> getAllFields() {
        return fieldRepository.findAll().stream()
                .map(this::mapToInfo)
                .collect(Collectors.toList());
    }

    @Override
    public List<UUID> getFieldIdsByOwner(UUID ownerId) {
        if (ownerId == null) return Collections.emptyList();
        return fieldRepository.findByOwnerId(ownerId).stream()
                .map(Field::getId)
                .collect(Collectors.toList());
    }

    private FieldParcelInfo mapToInfo(Field field) {
        BigDecimal lat = field.getLocation() != null ? field.getLocation().latitude() : null;
        BigDecimal lon = field.getLocation() != null ? field.getLocation().longitude() : null;

        return new FieldParcelInfo(
                field.getId(),
                field.getName(),
                field.getOwnerId(),
                field.getAreaHectares(),
                field.getCropType() != null ? field.getCropType().name() : "UNKNOWN",
                field.getSoilType() != null ? field.getSoilType().name() : "UNKNOWN",
                field.getIrrigationMethod() != null ? field.getIrrigationMethod().name() : "UNKNOWN",
                lat,
                lon
        );
    }
}
