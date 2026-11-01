package com.aquaai.anomaly.application.usecase;

import com.aquaai.anomaly.domain.model.AnomalyRuleConfig;
import com.aquaai.anomaly.domain.repository.AnomalyRuleConfigRepository;
import com.aquaai.shared.domain.ResourceNotFoundException;
import com.aquaai.shared.domain.UnauthorizedOperationException;
import com.aquaai.shared.security.CurrentUserProvider;
import com.aquaai.shared.security.UserRole;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ToggleAnomalyRuleUseCase {

    private final AnomalyRuleConfigRepository repository;
    private final CurrentUserProvider currentUserProvider;

    @Transactional
    public AnomalyRuleConfig execute(UUID id, boolean enabled) {
        if (id == null) {
            throw new ResourceNotFoundException("AnomalyRuleConfig", "null");
        }

        UserRole currentRole = currentUserProvider.getRequiredUserRole();
        if (currentRole != UserRole.ADMIN) {
            throw new UnauthorizedOperationException("Only ADMIN users are authorized to toggle anomaly rule status");
        }

        AnomalyRuleConfig config = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("AnomalyRuleConfig", id));

        config.setEnabled(enabled);
        AnomalyRuleConfig saved = repository.save(config);
        log.info("Anomaly rule config toggled: code={}, enabled={}", saved.getRuleCode(), saved.isEnabled());
        return saved;
    }
}
