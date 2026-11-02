package com.aquaai.prediction.application.usecase;

import com.aquaai.prediction.application.dto.GovernanceAuditLogDto;
import com.aquaai.prediction.domain.port.GovernanceAuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GetGovernanceAuditTrailUseCase {

    private final GovernanceAuditLogRepository auditLogRepository;

    @Transactional(readOnly = true)
    public List<GovernanceAuditLogDto> execute(int limit) {
        return auditLogRepository.findRecent(limit).stream()
                .map(GovernanceAuditLogDto::fromDomain)
                .collect(Collectors.toList());
    }
}
