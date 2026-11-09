package com.aquaai.admin.domain.repository;

import com.aquaai.admin.domain.model.Permission;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PermissionRepository {

    List<Permission> findAll();

    Optional<Permission> findById(UUID id);

    Optional<Permission> findByCode(String code);

    List<Permission> findByCategory(String category);
}
