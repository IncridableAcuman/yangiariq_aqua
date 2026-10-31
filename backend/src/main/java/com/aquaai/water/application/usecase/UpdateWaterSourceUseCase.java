package com.aquaai.water.application.usecase;

import com.aquaai.shared.domain.DuplicateResourceException;
import com.aquaai.shared.domain.ResourceNotFoundException;
import com.aquaai.shared.domain.UnauthorizedOperationException;
import com.aquaai.shared.security.CurrentUserProvider;
import com.aquaai.shared.security.UserRole;
import com.aquaai.water.api.dto.UpdateWaterSourceRequest;
import com.aquaai.water.api.dto.WaterSourceResponse;
import com.aquaai.water.domain.model.WaterSource;
import com.aquaai.water.domain.model.WaterSourceStatus;
import com.aquaai.water.domain.repository.WaterSourceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

/**
 * Use case: Updates regional water source details.
 * Restricted to OPERATOR and ADMIN roles.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UpdateWaterSourceUseCase {

    private final WaterSourceRepository waterSourceRepository;
    private final CurrentUserProvider currentUserProvider;

    @Transactional
    public WaterSourceResponse execute(UUID id, UpdateWaterSourceRequest request) {
        if (id == null) {
            throw new ResourceNotFoundException("WaterSource", "null");
        }
        UUID currentUserId = currentUserProvider.getRequiredUserId();
        UserRole currentRole = currentUserProvider.getRequiredUserRole();

        if (currentRole != UserRole.OPERATOR && currentRole != UserRole.ADMIN) {
            log.warn("User {} with role {} attempted unauthorized water source modification", currentUserId, currentRole);
            throw new UnauthorizedOperationException("Only district operators or administrators can update water sources");
        }

        WaterSource source = waterSourceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("WaterSource", id));

        String normalizedName = request.name() != null ? request.name().trim() : "";
        if (!source.getName().equalsIgnoreCase(normalizedName)) {
            Optional<WaterSource> existing = waterSourceRepository.findByNameIgnoreCaseAndStatus(normalizedName, WaterSourceStatus.ACTIVE);
            if (existing.isPresent() && !existing.get().getId().equals(id)) {
                throw new DuplicateResourceException("An active water source with name '" + normalizedName + "' already exists");
            }
        }

        source.updateDetails(request.name(), request.type(), request.description());
        WaterSource saved = waterSourceRepository.save(source);

        log.info("Water source {} updated by user {}", id, currentUserId);
        return WaterSourceResponse.from(saved);
    }
}
