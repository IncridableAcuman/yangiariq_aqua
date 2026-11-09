package com.aquaai.admin.application.service;

import com.aquaai.admin.application.dto.AdminDtos.*;
import com.aquaai.admin.domain.model.Permission;
import com.aquaai.admin.domain.model.Role;
import com.aquaai.admin.domain.repository.PermissionRepository;
import com.aquaai.admin.domain.repository.RoleRepository;
import com.aquaai.shared.domain.BusinessRuleException;
import com.aquaai.shared.domain.DuplicateResourceException;
import com.aquaai.shared.domain.ResourceNotFoundException;
import com.aquaai.shared.domain.UnauthorizedOperationException;
import com.aquaai.shared.security.SecurityContextHelper;
import com.aquaai.shared.security.UserRole;
import com.aquaai.user.domain.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoleAndPermissionService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final UserRepository userRepository;
    private final SecurityAuditService securityAuditService;

    @Transactional(readOnly = true)
    public List<RoleResponse> listRoles() {
        List<Role> roles = roleRepository.findAll();
        return roles.stream()
                .map(this::mapRoleResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public RoleResponse getRole(String roleCode) {
        String normalizedCode = roleCode.trim().toUpperCase();
        Role role = roleRepository.findByCode(normalizedCode)
                .orElseThrow(() -> new ResourceNotFoundException("Role", normalizedCode));
        return mapRoleResponse(role);
    }

    @Transactional
    public RoleResponse createRole(CreateRoleRequest request) {
        if (!SecurityContextHelper.isSuperAdmin()) {
            securityAuditService.logAction("ROLE_CREATE_FAILED", "ROLE", request.code(),
                    "Unauthorized attempt to create custom role by non-super-admin", "DENIED", null);
            throw new UnauthorizedOperationException("Only Super Administrators can define new platform roles");
        }

        String normalizedCode = request.code().trim().toUpperCase();
        if (roleRepository.existsByCode(normalizedCode)) {
            throw new DuplicateResourceException("Role", "code", normalizedCode);
        }

        // Validate all requested permissions exist in catalog
        Set<String> validPermissions = new HashSet<>();
        for (String permCode : request.permissions()) {
            if (permissionRepository.findByCode(permCode).isPresent()) {
                validPermissions.add(permCode);
            } else {
                throw new BusinessRuleException("Permission code does not exist in catalog: " + permCode, "INVALID_PERMISSION_CODE");
            }
        }

        Role newRole = Role.createCustom(
                normalizedCode,
                request.name(),
                request.description(),
                validPermissions
        );

        Role saved = roleRepository.save(newRole);

        securityAuditService.logAction("ROLE_CREATED", "ROLE", normalizedCode,
                "Created custom role with " + validPermissions.size() + " permissions", "SUCCESS", null);

        return mapRoleResponse(saved);
    }

    @Transactional
    public RoleResponse updateRolePermissions(String roleCode, UpdateRolePermissionsRequest request) {
        if (!SecurityContextHelper.isSuperAdmin() && !SecurityContextHelper.hasRole(UserRole.ADMIN)) {
            throw new UnauthorizedOperationException("Administrative authority required to manage role permissions");
        }

        String normalizedCode = roleCode.trim().toUpperCase();
        Role role = roleRepository.findByCode(normalizedCode)
                .orElseThrow(() -> new ResourceNotFoundException("Role", normalizedCode));

        if (role.isSystem() && "SUPER_ADMIN".equalsIgnoreCase(role.getCode())) {
            throw new BusinessRuleException("Permissions for the SUPER_ADMIN system role are immutable and cannot be revoked", "SUPER_ADMIN_IMMUTABLE");
        }

        // Non-super-admins cannot modify system roles
        if (role.isSystem() && !SecurityContextHelper.isSuperAdmin()) {
            securityAuditService.logAction("ROLE_PERMISSION_UPDATE_DENIED", "ROLE", normalizedCode,
                    "Admin attempted to mutate system role permissions without Super Admin role", "DENIED", null);
            throw new UnauthorizedOperationException("Only Super Administrators can modify system role permissions");
        }

        // Validate permissions
        Set<String> validPermissions = new HashSet<>();
        for (String permCode : request.permissions()) {
            if (permissionRepository.findByCode(permCode).isPresent()) {
                validPermissions.add(permCode);
            } else {
                throw new BusinessRuleException("Invalid permission code: " + permCode, "INVALID_PERMISSION_CODE");
            }
        }

        role.updatePermissions(validPermissions);
        Role saved = roleRepository.save(role);

        securityAuditService.logAction("ROLE_PERMISSIONS_UPDATED", "ROLE", normalizedCode,
                "Updated role permissions (assigned count: " + validPermissions.size() + ")", "SUCCESS", null);

        return mapRoleResponse(saved);
    }

    @Transactional
    public void deleteRole(String roleCode) {
        if (!SecurityContextHelper.isSuperAdmin()) {
            throw new UnauthorizedOperationException("Only Super Administrators can delete roles");
        }

        String normalizedCode = roleCode.trim().toUpperCase();
        Role role = roleRepository.findByCode(normalizedCode)
                .orElseThrow(() -> new ResourceNotFoundException("Role", normalizedCode));

        if (role.isSystem()) {
            throw new BusinessRuleException("System roles are protected and cannot be deleted", "SYSTEM_ROLE_PROTECTED");
        }

        roleRepository.delete(role);

        securityAuditService.logAction("ROLE_DELETED", "ROLE", normalizedCode,
                "Deleted custom role: " + normalizedCode, "SUCCESS", null);
    }

    @Transactional(readOnly = true)
    public List<PermissionResponse> listPermissions() {
        return permissionRepository.findAll().stream()
                .map(this::mapPermissionResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PermissionCategoryGroup> getPermissionsGroupedByCategory() {
        List<Permission> all = permissionRepository.findAll();
        Map<String, List<PermissionResponse>> grouped = all.stream()
                .map(this::mapPermissionResponse)
                .collect(Collectors.groupingBy(PermissionResponse::category));

        List<PermissionCategoryGroup> result = new ArrayList<>();
        grouped.forEach((category, permissions) -> {
            String label = switch (category) {
                case "USER_MANAGEMENT" -> "Foydalanuvchilar boshqaruvi / User Management";
                case "ROLE_MANAGEMENT" -> "Rollar va ruxsatlar / Roles & Permissions";
                case "FARM_FIELD_MANAGEMENT" -> "Fermer xoʻjaliklari va maydonlar / Farms & Fields";
                case "SENSOR_MANAGEMENT" -> "Datchiklar va telemetriya / Sensors & Telemetry";
                case "WATER_MANAGEMENT" -> "Suv taqsimoti va sugʻorish / Water & Irrigation";
                case "REPORTING" -> "Hisobotlar va eksport / Reporting & Analytics";
                case "GOVERNANCE" -> "Xavfsizlik va audit nazorati / Governance & Audit";
                case "PILOT_MANAGEMENT" -> "Dala tajribasi boshqaruvi / Field Pilot Management";
                case "ACCOUNTABILITY" -> "Hodisalar tekshiruvi / Incident Investigation";
                default -> category;
            };
            result.add(new PermissionCategoryGroup(category, label, permissions));
        });

        result.sort(Comparator.comparing(PermissionCategoryGroup::category));
        return result;
    }

    private RoleResponse mapRoleResponse(Role role) {
        long userCount = 0;
        try {
            UserRole userRole = UserRole.valueOf(role.getCode());
            userCount = userRepository.countByRole(userRole);
        } catch (IllegalArgumentException ignored) {
            // Custom role not in core UserRole enum
            userCount = 0;
        }

        return new RoleResponse(
                role.getId(),
                role.getCode(),
                role.getName(),
                role.getDescription(),
                role.isSystem(),
                role.getPermissions(),
                userCount,
                role.getCreatedAt(),
                role.getUpdatedAt()
        );
    }

    private PermissionResponse mapPermissionResponse(Permission p) {
        return new PermissionResponse(
                p.getId(),
                p.getCode(),
                p.getName(),
                p.getDescription(),
                p.getCategory()
        );
    }
}
