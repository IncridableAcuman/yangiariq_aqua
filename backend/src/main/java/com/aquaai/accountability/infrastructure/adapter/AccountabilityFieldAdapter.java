package com.aquaai.accountability.infrastructure.adapter;

import com.aquaai.accountability.domain.port.AccountabilityFieldPort;
import com.aquaai.field.domain.model.Field;
import com.aquaai.field.domain.repository.FieldRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AccountabilityFieldAdapter implements AccountabilityFieldPort {

    private final FieldRepository fieldRepository;

    @Override
    public boolean isFieldOwnedBy(UUID fieldId, UUID userId) {
        if (fieldId == null || userId == null) {
            return false;
        }
        return fieldRepository.findById(fieldId)
                .map(field -> userId.equals(field.getOwnerId()))
                .orElse(false);
    }

    @Override
    public Optional<String> getFieldName(UUID fieldId) {
        if (fieldId == null) {
            return Optional.empty();
        }
        return fieldRepository.findById(fieldId).map(Field::getName);
    }
}
