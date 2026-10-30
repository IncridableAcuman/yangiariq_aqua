package com.aquaai.field.application;

import com.aquaai.field.application.dto.CreateFieldCommand;
import com.aquaai.field.application.dto.FieldResult;
import com.aquaai.field.application.dto.UpdateFieldCommand;
import com.aquaai.field.domain.model.Field;
import com.aquaai.field.domain.model.FieldStatus;
import com.aquaai.field.domain.repository.FieldRepository;
import com.aquaai.shared.domain.DuplicateResourceException;
import com.aquaai.shared.domain.ResourceNotFoundException;
import com.aquaai.user.domain.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Application service orchestrating agricultural Field parcel use cases.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FieldService {

    private final FieldRepository fieldRepository;
    private final FieldOwnershipChecker ownershipChecker;
    private final UserRepository userRepository;

    /**
     * Registers a new agricultural field parcel for a farmer.
     */
    @Transactional
    public FieldResult createField(CreateFieldCommand command) {
        log.info("Creating new field '{}' for owner {}", command.name(), command.ownerId());

        if (command.ownerId() == null) {
            throw new ResourceNotFoundException("User", "null");
        }
        if (!userRepository.existsById(command.ownerId())) {
            throw new ResourceNotFoundException("User", command.ownerId());
        }

        String normalizedName = command.name() != null ? command.name().trim() : "";
        if (fieldRepository.existsByOwnerIdAndNameIgnoreCaseAndStatus(command.ownerId(), normalizedName, FieldStatus.ACTIVE)) {
            throw new DuplicateResourceException("An active field with name '" + normalizedName + "' already exists for this farmer");
        }

        Field field = Field.create(
                command.ownerId(),
                command.name(),
                command.description(),
                command.areaHectares(),
                command.cropType(),
                command.soilType(),
                command.irrigationMethod(),
                command.location(),
                command.boundary()
        );

        Field saved = fieldRepository.save(field);
        log.info("Field '{}' created successfully with ID: {}", saved.getName(), saved.getId());
        return FieldResult.from(saved);
    }

    /**
     * Updates an existing field parcel's details with ownership verification.
     */
    @Transactional
    public FieldResult updateField(UpdateFieldCommand command) {
        log.info("Updating field {} by user {}", command.fieldId(), command.currentUserId());

        ownershipChecker.verifyOwnership(command.fieldId(), command.currentUserId());

        Field field = fieldRepository.findById(command.fieldId())
                .orElseThrow(() -> new ResourceNotFoundException("Field", command.fieldId()));

        String normalizedName = command.name() != null ? command.name().trim() : "";
        if (fieldRepository.existsByOwnerIdAndNameIgnoreCaseAndStatusAndIdNot(
                field.getOwnerId(), normalizedName, FieldStatus.ACTIVE, field.getId())) {
            throw new DuplicateResourceException("An active field with name '" + normalizedName + "' already exists for this farmer");
        }

        field.updateDetails(
                command.name(),
                command.description(),
                command.areaHectares(),
                command.cropType(),
                command.soilType(),
                command.irrigationMethod(),
                command.location(),
                command.boundary()
        );

        Field updated = fieldRepository.save(field);
        log.info("Field {} updated successfully", updated.getId());
        return FieldResult.from(updated);
    }

    /**
     * Retrieves a field by its ID, enforcing ownership check.
     */
    @Transactional(readOnly = true)
    public FieldResult getFieldById(UUID fieldId, UUID currentUserId) {
        log.debug("Fetching field {} for user {}", fieldId, currentUserId);
        ownershipChecker.verifyOwnership(fieldId, currentUserId);

        Field field = fieldRepository.findById(fieldId)
                .orElseThrow(() -> new ResourceNotFoundException("Field", fieldId));

        return FieldResult.from(field);
    }

    /**
     * Lists all fields owned by a specific farmer.
     */
    @Transactional(readOnly = true)
    public List<FieldResult> listFieldsByOwner(UUID ownerId) {
        log.debug("Listing all fields for owner {}", ownerId);
        return fieldRepository.findByOwnerId(ownerId)
                .stream()
                .map(FieldResult::from)
                .toList();
    }

    /**
     * Lists active fields owned by a specific farmer.
     */
    @Transactional(readOnly = true)
    public List<FieldResult> listActiveFieldsByOwner(UUID ownerId) {
        log.debug("Listing active fields for owner {}", ownerId);
        return fieldRepository.findByOwnerIdAndStatus(ownerId, FieldStatus.ACTIVE)
                .stream()
                .map(FieldResult::from)
                .toList();
    }

    /**
     * Deactivates a field parcel.
     */
    @Transactional
    public void deactivateField(UUID fieldId, UUID currentUserId) {
        log.info("Deactivating field {} by user {}", fieldId, currentUserId);
        ownershipChecker.verifyOwnership(fieldId, currentUserId);

        Field field = fieldRepository.findById(fieldId)
                .orElseThrow(() -> new ResourceNotFoundException("Field", fieldId));

        field.deactivate();
        fieldRepository.save(field);
        log.info("Field {} deactivated", fieldId);
    }

    /**
     * Reactivates a deactivated field parcel.
     */
    @Transactional
    public void activateField(UUID fieldId, UUID currentUserId) {
        log.info("Activating field {} by user {}", fieldId, currentUserId);
        ownershipChecker.verifyOwnership(fieldId, currentUserId);

        Field field = fieldRepository.findById(fieldId)
                .orElseThrow(() -> new ResourceNotFoundException("Field", fieldId));

        if (fieldRepository.existsByOwnerIdAndNameIgnoreCaseAndStatusAndIdNot(
                field.getOwnerId(), field.getName(), FieldStatus.ACTIVE, field.getId())) {
            throw new DuplicateResourceException("An active field with name '" + field.getName() + "' already exists for this farmer");
        }

        field.activate();
        fieldRepository.save(field);
        log.info("Field {} reactivated", fieldId);
    }
}
