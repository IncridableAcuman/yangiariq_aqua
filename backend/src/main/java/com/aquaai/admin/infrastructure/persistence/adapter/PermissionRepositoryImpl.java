package com.aquaai.admin.infrastructure.persistence.adapter;

import com.aquaai.admin.domain.model.Permission;
import com.aquaai.admin.domain.repository.PermissionRepository;
import com.aquaai.admin.infrastructure.persistence.mapper.RoleEntityMapper;
import com.aquaai.admin.infrastructure.persistence.repository.SpringDataPermissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class PermissionRepositoryImpl implements PermissionRepository {

    private final SpringDataPermissionRepository springDataPermissionRepository;

    @Override
    public List<Permission> findAll() {
        return springDataPermissionRepository.findAll().stream()
                .map(RoleEntityMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Permission> findById(UUID id) {
        return springDataPermissionRepository.findById(id)
                .map(RoleEntityMapper::toDomain);
    }

    @Override
    public Optional<Permission> findByCode(String code) {
        return springDataPermissionRepository.findByCode(code)
                .map(RoleEntityMapper::toDomain);
    }

    @Override
    public List<Permission> findByCategory(String category) {
        return springDataPermissionRepository.findByCategory(category).stream()
                .map(RoleEntityMapper::toDomain)
                .collect(Collectors.toList());
    }
}
