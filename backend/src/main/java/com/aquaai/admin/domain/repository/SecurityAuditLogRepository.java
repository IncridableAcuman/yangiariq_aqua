package com.aquaai.admin.domain.repository;

import com.aquaai.admin.domain.model.SecurityAuditLog;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SecurityAuditLogRepository {

    SecurityAuditLog save(SecurityAuditLog auditLog);

    Optional<SecurityAuditLog> findById(UUID id);

    List<SecurityAuditLog> findWithFilters(String action, String targetType, String result, int page, int size);

    long countWithFilters(String action, String targetType, String result);
}
