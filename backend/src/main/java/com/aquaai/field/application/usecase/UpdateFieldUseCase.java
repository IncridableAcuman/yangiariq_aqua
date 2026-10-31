package com.aquaai.field.application.usecase;

import com.aquaai.field.api.dto.FieldResponse;
import com.aquaai.field.api.dto.UpdateFieldRequest;
import com.aquaai.field.application.FieldOwnershipChecker;
import com.aquaai.field.domain.model.CropType;
import com.aquaai.field.domain.model.Field;
import com.aquaai.field.domain.model.FieldBoundary;
import com.aquaai.field.domain.model.FieldLocation;
import com.aquaai.field.domain.model.FieldStatus;
import com.aquaai.field.domain.model.IrrigationMethod;
import com.aquaai.field.domain.model.SoilType;
import com.aquaai.field.domain.repository.FieldRepository;
import com.aquaai.shared.domain.DuplicateResourceException;
import com.aquaai.shared.domain.ResourceNotFoundException;
import com.aquaai.shared.security.CurrentUserProvider;
import com.aquaai.shared.security.UserRole;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Use case: Modifies existing field parcel details using PATCH semantics.
 * Unspecified request properties remain unchanged.
 * Prevents modifying ownerId, id, status, or timestamps.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UpdateFieldUseCase {

    private final FieldRepository fieldRepository;
    private final FieldOwnershipChecker ownershipChecker;
    private final CurrentUserProvider currentUserProvider;

    @Transactional
    public FieldResponse execute(UUID fieldId, UpdateFieldRequest request) {
        UUID currentUserId = currentUserProvider.getRequiredUserId();
        UserRole currentRole = currentUserProvider.getRequiredUserRole();

        Field field = ownershipChecker.getFieldForMutation(fieldId, currentUserId, currentRole);

        // PATCH semantics: Unspecified properties retain their current values
        String newName = (request.name() != null && !request.name().isBlank())
                ? request.name().trim()
                : field.getName();

        String newDescription = request.description() != null
                ? request.description()
                : field.getDescription();

        BigDecimal newArea = request.areaHectares() != null
                ? request.areaHectares()
                : field.getAreaHectares();

        CropType newCropType = request.cropType() != null
                ? request.cropType()
                : field.getCropType();

        SoilType newSoilType = request.soilType() != null
                ? request.soilType()
                : field.getSoilType();

        IrrigationMethod newIrrigationMethod = request.irrigationMethod() != null
                ? request.irrigationMethod()
                : field.getIrrigationMethod();

        FieldLocation newLocation = field.getLocation();
        if (request.latitude() != null || request.longitude() != null) {
            BigDecimal lat = request.latitude() != null
                    ? request.latitude()
                    : (field.getLocation() != null ? field.getLocation().latitude() : null);
            BigDecimal lon = request.longitude() != null
                    ? request.longitude()
                    : (field.getLocation() != null ? field.getLocation().longitude() : null);
            newLocation = (lat != null && lon != null) ? FieldLocation.of(lat, lon) : null;
        }

        FieldBoundary newBoundary = request.boundary() != null
                ? request.boundary()
                : field.getBoundary();

        // Check duplicate name for owner if name is changing
        if (!newName.equalsIgnoreCase(field.getName())) {
            if (fieldRepository.existsByOwnerIdAndNameIgnoreCaseAndStatusAndIdNot(
                    field.getOwnerId(), newName, FieldStatus.ACTIVE, field.getId())) {
                throw new DuplicateResourceException("An active field with name '" + newName + "' already exists for this farmer");
            }
        }

        field.updateDetails(
                newName,
                newDescription,
                newArea,
                newCropType,
                newSoilType,
                newIrrigationMethod,
                newLocation,
                newBoundary
        );

        Field updated = fieldRepository.save(field);
        log.info("Field {} updated successfully by user {}", fieldId, currentUserId);
        return FieldResponse.from(updated);
    }
}
