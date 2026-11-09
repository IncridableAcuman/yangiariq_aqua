package com.aquaai.admin.infrastructure.persistence.adapter;

import com.aquaai.admin.domain.model.SecurityAuditLog;
import com.aquaai.admin.domain.repository.SecurityAuditLogRepository;
import com.aquaai.admin.infrastructure.persistence.entity.SecurityAuditLogJpaEntity;
import com.aquaai.admin.infrastructure.persistence.mapper.SecurityAuditLogMapper;
import com.aquaai.admin.infrastructure.persistence.repository.SpringDataSecurityAuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class SecurityAuditLogRepositoryImpl implements SecurityAuditLogRepository {

    private final SpringDataSecurityAuditLogRepository springDataRepository;

    @Override
    public SecurityAuditLog save(SecurityAuditLog auditLog) {
        SecurityAuditLogJpaEntity entity = SecurityAuditLogMapper.toEntity(auditLog);
        SecurityAuditLogJpaEntity saved = springDataRepository.save(entity);
        return SecurityAuditLogMapper.toDomain(saved);
    }

    @Override
    public Optional<SecurityAuditLog> findById(UUID id) {
        return springDataRepository.findById(id)
                .map(SecurityAuditLogMapper::toDomain);
    }

    @Override
    public List<SecurityAuditLog> findWithFilters(String action, String targetType, String result, int page, int size) {
        Page<SecurityAuditLogJpaEntity> p = springDataRepository.findWithFilters(
                action,
                targetType,
                result,
                PageRequest.of(page, size)
        );
        return p.getContent().stream()
                .map(SecurityAuditLogMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public long countWithFilters(String action, String targetType, String result) {
        return springDataRepository.countWithFilters(action, targetType, result);
    }
}
