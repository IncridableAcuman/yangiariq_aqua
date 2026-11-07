package com.aquaai.wateraccounting.infrastructure.adapter;

import com.aquaai.field.domain.model.Field;
import com.aquaai.field.domain.repository.FieldRepository;
import com.aquaai.wateraccounting.domain.port.AccountingFieldPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class AccountingFieldAdapter implements AccountingFieldPort {

    private final FieldRepository fieldRepository;

    @Override
    public List<FieldSummary> getAllFields() {
        return fieldRepository.findAll().stream()
                .map(this::mapToSummary)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<FieldSummary> getFieldById(UUID fieldId) {
        return fieldRepository.findById(fieldId).map(this::mapToSummary);
    }

    @Override
    public List<FieldSummary> getFieldsByOwner(UUID ownerId) {
        return fieldRepository.findByOwnerId(ownerId).stream()
                .map(this::mapToSummary)
                .collect(Collectors.toList());
    }

    private FieldSummary mapToSummary(Field f) {
        return new FieldSummary(
                f.getId(),
                f.getName(),
                f.getOwnerId(),
                f.getAreaHectares(),
                f.getCropType() != null ? f.getCropType().name() : "OTHER",
                f.getSoilType() != null ? f.getSoilType().name() : "LOAM"
        );
    }
}
