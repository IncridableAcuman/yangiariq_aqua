package com.aquaai.water.application.usecase;

import com.aquaai.shared.domain.ResourceNotFoundException;
import com.aquaai.shared.domain.UnauthorizedOperationException;
import com.aquaai.shared.security.CurrentUserProvider;
import com.aquaai.shared.security.UserRole;
import com.aquaai.water.api.dto.WaterSourceResponse;
import com.aquaai.water.domain.model.WaterSource;
import com.aquaai.water.domain.repository.WaterSourceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Use case: Activates a water source infrastructure entity.
 * Restricted to OPERATOR and ADMIN roles.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ActivateWaterSourceUseCase {

    private final WaterSourceRepository waterSourceRepository;
    private final CurrentUserProvider currentUserProvider;

    @Transactional
    public WaterSourceResponse execute(UUID id) {
        if (id == null) {
            throw new ResourceNotFoundException("WaterSource", "null");
        }
        UUID currentUserId = currentUserProvider.getRequiredUserId();
        UserRole currentRole = currentUserProvider.getRequiredUserRole();

        if (currentRole != UserRole.OPERATOR && currentRole != UserRole.ADMIN) {
            log.warn("User {} with role {} attempted unauthorized water source activation", currentUserId, currentRole);
            throw new UnauthorizedOperationException("Only district operators or administrators can activate water sources");
        }

        WaterSource source = waterSourceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("WaterSource", id));

        source.activate();
        WaterSource saved = waterSourceRepository.save(source);

        log.info("Water source {} activated by user {}", id, currentUserId);
        return WaterSourceResponse.from(saved);
    }
}
