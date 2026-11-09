package com.aquaai.admin.application.service;

import com.aquaai.admin.application.dto.AdminDtos.SecurityAuditLogResponse;
import com.aquaai.admin.domain.model.SecurityAuditLog;
import com.aquaai.admin.domain.repository.SecurityAuditLogRepository;
import com.aquaai.shared.api.PageResponse;
import com.aquaai.shared.security.SecurityContextHelper;
import com.aquaai.shared.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SecurityAuditService {

    private final SecurityAuditLogRepository securityAuditLogRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logAction(
            String action,
            String targetType,
            String targetId,
            String detailsJson,
            String result,
            String ipAddress
    ) {
        UUID actorId = null;
        String actorName = "SYSTEM";
        String actorRole = "SYSTEM";

        Optional<UserPrincipal> principalOpt = SecurityContextHelper.getCurrentPrincipal();
        if (principalOpt.isPresent()) {
            UserPrincipal principal = principalOpt.get();
            actorId = principal.getId();
            actorName = principal.getUsername();
            actorRole = principal.getRole() != null ? principal.getRole().name() : "UNKNOWN";
        }

        // Sensitive information sanitation guard: Ensure passwords and tokens never persist in audit logs
        String sanitizedDetails = detailsJson;
        if (sanitizedDetails != null) {
            sanitizedDetails = sanitizedDetails
                    .replaceAll("(?i)\"password\"\\s*:\\s*\"[^\"]*\"", "\"password\":\"***REDACTED***\"")
                    .replaceAll("(?i)\"token\"\\s*:\\s*\"[^\"]*\"", "\"token\":\"***REDACTED***\"")
                    .replaceAll("(?i)\"passwordHash\"\\s*:\\s*\"[^\"]*\"", "\"passwordHash\":\"***REDACTED***\"");
        }

        SecurityAuditLog auditLog = SecurityAuditLog.create(
                actorId,
                actorName,
                actorRole,
                action,
                targetType,
                targetId,
                sanitizedDetails,
                result,
                ipAddress
        );

        try {
            securityAuditLogRepository.save(auditLog);
            log.info("SECURITY AUDIT: [{}] {} on {} (ID: {}) by {} -> {}",
                    result, action, targetType, targetId, actorName, result);
        } catch (Exception e) {
            log.error("Failed to persist security audit record: {}", e.getMessage(), e);
        }
    }

    @Transactional(readOnly = true)
    public PageResponse<SecurityAuditLogResponse> getAuditLogs(
            String action,
            String targetType,
            String result,
            int page,
            int size
    ) {
        var list = securityAuditLogRepository.findWithFilters(action, targetType, result, page, size);
        long total = securityAuditLogRepository.countWithFilters(action, targetType, result);
        var mapped = list.stream().map(l -> new SecurityAuditLogResponse(
                l.getId(),
                l.getActorId(),
                l.getActorName(),
                l.getActorRole(),
                l.getAction(),
                l.getTargetType(),
                l.getTargetId(),
                l.getDetailsJson(),
                l.getResult(),
                l.getIpAddress(),
                l.getCreatedAt()
        )).toList();
        int totalPages = (int) Math.ceil((double) total / (double) size);
        boolean isLast = (page + 1) >= totalPages;

        return PageResponse.<SecurityAuditLogResponse>builder()
                .content(mapped)
                .page(page)
                .size(size)
                .totalElements(total)
                .totalPages(totalPages)
                .last(isLast)
                .build();
    }
}
