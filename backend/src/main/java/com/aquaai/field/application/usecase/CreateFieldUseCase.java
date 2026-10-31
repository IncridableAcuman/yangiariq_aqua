package com.aquaai.field.application.usecase;

import com.aquaai.field.api.dto.CreateFieldRequest;
import com.aquaai.field.api.dto.FieldResponse;
import com.aquaai.field.domain.model.Field;
import com.aquaai.field.domain.model.FieldLocation;
import com.aquaai.field.domain.model.FieldStatus;
import com.aquaai.field.domain.repository.FieldRepository;
import com.aquaai.shared.domain.DuplicateResourceException;
import com.aquaai.shared.domain.ResourceNotFoundException;
import com.aquaai.shared.domain.UnauthorizedOperationException;
import com.aquaai.shared.security.CurrentUserProvider;
import com.aquaai.shared.security.UserRole;
import com.aquaai.user.domain.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Use case: Registers a new agricultural field parcel for the authenticated farmer.
 * Enforces ownership identity from security context and role authorization.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CreateFieldUseCase {

    private final FieldRepository fieldRepository;
    private final UserRepository userRepository;
    private final CurrentUserProvider currentUserProvider;

    @Transactional
    public FieldResponse execute(CreateFieldRequest request) {
        UUID currentUserId = currentUserProvider.getRequiredUserId();
        UserRole currentRole = currentUserProvider.getRequiredUserRole();

        if (currentRole != UserRole.FARMER) {
            log.warn("User {} with role {} attempted to create a field parcel", currentUserId, currentRole);
            throw new UnauthorizedOperationException("Only registered farmers can create agricultural field parcels");
        }

        if (!userRepository.existsById(currentUserId)) {
            throw new ResourceNotFoundException("User", currentUserId);
        }

        String normalizedName = request.name() != null ? request.name().trim() : "";
        if (fieldRepository.existsByOwnerIdAndNameIgnoreCaseAndStatus(currentUserId, normalizedName, FieldStatus.ACTIVE)) {
            throw new DuplicateResourceException("An active field with name '" + normalizedName + "' already exists for this farmer");
        }

        FieldLocation location = null;
        if (request.latitude() != null && request.longitude() != null) {
            location = FieldLocation.of(request.latitude(), request.longitude());
        }

        Field field = Field.create(
                currentUserId,
                request.name(),
                request.description(),
                request.areaHectares(),
                request.cropType(),
                request.soilType(),
                request.irrigationMethod(),
                location,
                request.boundary()
        );

        Field saved = fieldRepository.save(field);
        log.info("Field '{}' created successfully with ID: {} for farmer {}", saved.getName(), saved.getId(), currentUserId);
        return FieldResponse.from(saved);
    }
}
