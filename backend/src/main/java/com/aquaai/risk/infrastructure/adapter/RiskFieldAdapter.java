package com.aquaai.risk.infrastructure.adapter;

import com.aquaai.field.domain.model.Field;
import com.aquaai.field.domain.repository.FieldRepository;
import com.aquaai.risk.domain.port.RiskFieldPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class RiskFieldAdapter implements RiskFieldPort {

    private final FieldRepository fieldRepository;

    @Override
    public List<UUID> getAccessibleFieldIds(UUID currentUserId, boolean isDistrictScope) {
        if (isDistrictScope) {
            return fieldRepository.findAll().stream()
                    .map(Field::getId)
                    .toList();
        }
        if (currentUserId == null) {
            return Collections.emptyList();
        }
        return fieldRepository.findByOwnerId(currentUserId).stream()
                .map(Field::getId)
                .toList();
    }

    @Override
    public Optional<FieldInfo> getFieldInfo(UUID fieldId) {
        if (fieldId == null) return Optional.empty();
        return fieldRepository.findById(fieldId).map(this::mapToInfo);
    }

    @Override
    public List<FieldInfo> getAllAccessibleFieldInfos(UUID currentUserId, boolean isDistrictScope) {
        List<Field> fields;
        if (isDistrictScope) {
            fields = fieldRepository.findAll();
        } else {
            if (currentUserId == null) return Collections.emptyList();
            fields = fieldRepository.findByOwnerId(currentUserId);
        }
        return fields.stream().map(this::mapToInfo).toList();
    }

    private FieldInfo mapToInfo(Field f) {
        return new FieldInfo(
                f.getId(),
                f.getOwnerId(),
                f.getName(),
                f.getAreaHectares(),
                f.getCropType() != null ? f.getCropType().name() : null,
                f.getSoilType() != null ? f.getSoilType().name() : null,
                f.getIrrigationMethod() != null ? f.getIrrigationMethod().name() : null
        );
    }
}
