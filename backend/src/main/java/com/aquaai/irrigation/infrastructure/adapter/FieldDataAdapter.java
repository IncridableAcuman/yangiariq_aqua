package com.aquaai.irrigation.infrastructure.adapter;

import com.aquaai.field.domain.model.Field;
import com.aquaai.field.domain.repository.FieldRepository;
import com.aquaai.irrigation.domain.port.FieldDataPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class FieldDataAdapter implements FieldDataPort {

    private final FieldRepository fieldRepository;

    @Override
    public Optional<FieldAgronomicData> getFieldData(UUID fieldId) {
        if (fieldId == null) {
            return Optional.empty();
        }
        return fieldRepository.findById(fieldId).map(this::mapField);
    }

    private FieldAgronomicData mapField(Field field) {
        return new FieldAgronomicData(
                field.getId(),
                field.getName(),
                field.getOwnerId(),
                field.getAreaHectares(),
                field.getCropType() != null ? field.getCropType().name() : null,
                field.getSoilType() != null ? field.getSoilType().name() : null,
                field.getIrrigationMethod() != null ? field.getIrrigationMethod().name() : null
        );
    }
}
