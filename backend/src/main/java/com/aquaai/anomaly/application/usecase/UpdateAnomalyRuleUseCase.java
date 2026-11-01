package com.aquaai.anomaly.application.usecase;

import com.aquaai.anomaly.domain.model.AnomalyRuleConfig;
import com.aquaai.anomaly.domain.model.AnomalySeverity;
import com.aquaai.anomaly.domain.repository.AnomalyRuleConfigRepository;
import com.aquaai.shared.domain.ResourceNotFoundException;
import com.aquaai.shared.domain.UnauthorizedOperationException;
import com.aquaai.shared.security.CurrentUserProvider;
import com.aquaai.shared.security.UserRole;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UpdateAnomalyRuleUseCase {

    private final AnomalyRuleConfigRepository repository;
    private final CurrentUserProvider currentUserProvider;

    public record Command(
            UUID id,
            String name,
            String description,
            AnomalySeverity severity,
            BigDecimal threshold,
            BigDecimal secondaryThreshold,
            int timeWindowMinutes,
            int minConsecutiveReadings
    ) {
    }

    @Transactional
    public AnomalyRuleConfig execute(Command command) {
        if (command.id() == null) {
            throw new ResourceNotFoundException("AnomalyRuleConfig", "null");
        }

        UserRole currentRole = currentUserProvider.getRequiredUserRole();
        if (currentRole != UserRole.ADMIN) {
            throw new UnauthorizedOperationException("Only ADMIN users are authorized to modify global anomaly rule configurations");
        }

        AnomalyRuleConfig config = repository.findById(command.id())
                .orElseThrow(() -> new ResourceNotFoundException("AnomalyRuleConfig", command.id()));

        config.update(
                command.name(),
                command.description(),
                command.severity(),
                command.threshold(),
                command.secondaryThreshold(),
                command.timeWindowMinutes(),
                command.minConsecutiveReadings()
        );

        AnomalyRuleConfig saved = repository.save(config);
        log.info("Anomaly rule config updated: code={}, threshold={}", saved.getRuleCode(), saved.getThreshold());
        return saved;
    }
}
