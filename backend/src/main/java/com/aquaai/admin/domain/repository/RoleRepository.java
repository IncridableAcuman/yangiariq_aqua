package com.aquaai.admin.domain.repository;

import com.aquaai.admin.domain.model.Role;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RoleRepository {

    List<Role> findAll();

    Optional<Role> findById(UUID id);

    Optional<Role> findByCode(String code);

    boolean existsByCode(String code);

    Role save(Role role);

    void delete(Role role);
}
