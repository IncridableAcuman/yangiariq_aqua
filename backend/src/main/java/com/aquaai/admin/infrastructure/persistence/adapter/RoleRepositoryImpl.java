package com.aquaai.admin.infrastructure.persistence.adapter;

import com.aquaai.admin.domain.model.Role;
import com.aquaai.admin.domain.repository.RoleRepository;
import com.aquaai.admin.infrastructure.persistence.entity.PermissionJpaEntity;
import com.aquaai.admin.infrastructure.persistence.entity.RoleJpaEntity;
import com.aquaai.admin.infrastructure.persistence.mapper.RoleEntityMapper;
import com.aquaai.admin.infrastructure.persistence.repository.SpringDataPermissionRepository;
import com.aquaai.admin.infrastructure.persistence.repository.SpringDataRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class RoleRepositoryImpl implements RoleRepository {

    private final SpringDataRoleRepository springDataRoleRepository;
    private final SpringDataPermissionRepository springDataPermissionRepository;

    @Override
    public List<Role> findAll() {
        return springDataRoleRepository.findAll().stream()
                .map(RoleEntityMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Role> findById(UUID id) {
        return springDataRoleRepository.findById(id)
                .map(RoleEntityMapper::toDomain);
    }

    @Override
    public Optional<Role> findByCode(String code) {
        return springDataRoleRepository.findByCode(code)
                .map(RoleEntityMapper::toDomain);
    }

    @Override
    public boolean existsByCode(String code) {
        return springDataRoleRepository.existsByCode(code);
    }

    @Override
    public Role save(Role role) {
        Set<PermissionJpaEntity> permissionEntities = new HashSet<>();
        if (role.getPermissions() != null) {
            for (String code : role.getPermissions()) {
                springDataPermissionRepository.findByCode(code).ifPresent(permissionEntities::add);
            }
        }
        RoleJpaEntity entity = RoleEntityMapper.toEntity(role, permissionEntities);
        RoleJpaEntity saved = springDataRoleRepository.save(entity);
        return RoleEntityMapper.toDomain(saved);
    }

    @Override
    public void delete(Role role) {
        springDataRoleRepository.deleteById(role.getId());
    }
}
