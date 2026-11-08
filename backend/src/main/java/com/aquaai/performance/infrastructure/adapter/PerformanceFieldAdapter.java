package com.aquaai.performance.infrastructure.adapter;

import com.aquaai.field.domain.repository.FieldRepository;
import com.aquaai.performance.domain.port.PerformanceFieldPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PerformanceFieldAdapter implements PerformanceFieldPort {

    private final FieldRepository fieldRepository;

    @Override
    public boolean isFieldOwnedBy(UUID fieldId, UUID userId) {
        if (fieldId == null || userId == null) return false;
        return fieldRepository.findByIdAndOwnerId(fieldId, userId).isPresent();
    }

    @Override
    public String getFieldName(UUID fieldId) {
        if (fieldId == null) return "Unknown Field";
        return fieldRepository.findById(fieldId)
                .map(f -> f.getName())
                .orElse("Field " + fieldId.toString().substring(0, 8));
    }
}
