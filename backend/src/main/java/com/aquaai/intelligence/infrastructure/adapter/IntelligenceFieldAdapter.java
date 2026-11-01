package com.aquaai.intelligence.infrastructure.adapter;

import com.aquaai.field.domain.model.Field;
import com.aquaai.field.domain.repository.FieldRepository;
import com.aquaai.intelligence.domain.port.IntelligenceFieldPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class IntelligenceFieldAdapter implements IntelligenceFieldPort {

    private final FieldRepository fieldRepository;

    @Override
    public Optional<ParcelDetails> getFieldDetails(UUID fieldId) {
        if (fieldId == null) return Optional.empty();
        return fieldRepository.findById(fieldId)
                .map(f -> new ParcelDetails(
                        f.getId(),
                        f.getOwnerId(),
                        f.getCropType() != null ? f.getCropType().name() : "COTTON",
                        f.getSoilType() != null ? f.getSoilType().name() : "LOAM",
                        f.getAreaHectares()
                ));
    }
}
