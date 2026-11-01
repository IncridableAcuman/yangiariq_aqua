package com.aquaai.water.application.usecase;

import com.aquaai.shared.domain.DuplicateResourceException;
import com.aquaai.shared.domain.UnauthorizedOperationException;
import com.aquaai.shared.security.CurrentUserProvider;
import com.aquaai.shared.security.UserRole;
import com.aquaai.water.api.dto.CreateWaterSourceRequest;
import com.aquaai.water.api.dto.WaterSourceResponse;
import com.aquaai.water.domain.model.WaterSource;
import com.aquaai.water.domain.model.WaterSourceStatus;
import com.aquaai.water.domain.repository.WaterSourceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Use case: Registers a new regional water source infrastructure entity.
 * Restricted to OPERATOR and ADMIN roles.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CreateWaterSourceUseCase {

    private final WaterSourceRepository waterSourceRepository;
    private final CurrentUserProvider currentUserProvider;

    @Transactional
    public WaterSourceResponse execute(CreateWaterSourceRequest request) {
        UUID currentUserId = currentUserProvider.getRequiredUserId();
        UserRole currentRole = currentUserProvider.getRequiredUserRole();

        if (currentRole != UserRole.OPERATOR && currentRole != UserRole.ADMIN) {
            log.warn("User {} with role {} attempted unauthorized water source registration", currentUserId, currentRole);
            throw new UnauthorizedOperationException("Only district operators or administrators can register water sources");
        }

        String normalizedName = request.name() != null ? request.name().trim() : "";
        if (waterSourceRepository.existsByNameIgnoreCaseAndStatus(normalizedName, WaterSourceStatus.ACTIVE)) {
            throw new DuplicateResourceException("An active water source with name '" + normalizedName + "' already exists");
        }

        WaterSource source = WaterSource.create(
                request.name(),
                request.type(),
                request.description(),
                request.latitude(),
                request.longitude()
        );
        WaterSource saved = waterSourceRepository.save(source);

        log.info("Water source '{}' created with ID {} by user {}", saved.getName(), saved.getId(), currentUserId);
        return WaterSourceResponse.from(saved);
    }
}
