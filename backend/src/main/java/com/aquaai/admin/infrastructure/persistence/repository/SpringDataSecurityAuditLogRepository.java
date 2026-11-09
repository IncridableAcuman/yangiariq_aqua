package com.aquaai.admin.infrastructure.persistence.repository;

import com.aquaai.admin.infrastructure.persistence.entity.SecurityAuditLogJpaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface SpringDataSecurityAuditLogRepository extends JpaRepository<SecurityAuditLogJpaEntity, UUID> {

    @Query("""
        SELECT a FROM SecurityAuditLogJpaEntity a
        WHERE (:action IS NULL OR :action = '' OR a.action = :action)
          AND (:targetType IS NULL OR :targetType = '' OR a.targetType = :targetType)
          AND (:result IS NULL OR :result = '' OR a.result = :result)
        ORDER BY a.createdAt DESC
    """)
    Page<SecurityAuditLogJpaEntity> findWithFilters(
            @Param("action") String action,
            @Param("targetType") String targetType,
            @Param("result") String result,
            Pageable pageable
    );

    @Query("""
        SELECT COUNT(a) FROM SecurityAuditLogJpaEntity a
        WHERE (:action IS NULL OR :action = '' OR a.action = :action)
          AND (:targetType IS NULL OR :targetType = '' OR a.targetType = :targetType)
          AND (:result IS NULL OR :result = '' OR a.result = :result)
    """)
    long countWithFilters(
            @Param("action") String action,
            @Param("targetType") String targetType,
            @Param("result") String result
    );
}
