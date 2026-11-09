package com.aquaai.admin.infrastructure.persistence.repository;

import com.aquaai.admin.infrastructure.persistence.entity.PermissionJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpringDataPermissionRepository extends JpaRepository<PermissionJpaEntity, UUID> {

    Optional<PermissionJpaEntity> findByCode(String code);

    List<PermissionJpaEntity> findByCategory(String category);
}
