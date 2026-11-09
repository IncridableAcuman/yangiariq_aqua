package com.aquaai.user.application;

import com.aquaai.admin.application.service.SecurityAuditService;
import com.aquaai.shared.api.PageResponse;
import com.aquaai.shared.domain.BusinessRuleException;
import com.aquaai.shared.domain.DuplicateResourceException;
import com.aquaai.shared.domain.ResourceNotFoundException;
import com.aquaai.shared.domain.UnauthorizedOperationException;
import com.aquaai.shared.security.SecurityContextHelper;
import com.aquaai.shared.security.UserRole;
import com.aquaai.user.api.dto.AdminCreateUserRequest;
import com.aquaai.user.api.dto.AdminUpdateUserRequest;
import com.aquaai.user.api.dto.ChangeUserRoleRequest;
import com.aquaai.user.api.dto.ChangeUserStatusRequest;
import com.aquaai.user.api.dto.UpdateUserProfileRequest;
import com.aquaai.user.api.dto.UserProfileResponse;
import com.aquaai.user.api.dto.UserSummaryStatsResponse;
import com.aquaai.user.domain.User;
import com.aquaai.user.domain.UserRepository;
import com.aquaai.user.domain.UserStatus;
import com.aquaai.user.domain.event.UserSecurityInvalidationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final PasswordEncoder passwordEncoder;
    private final SecurityAuditService securityAuditService;

    @Transactional(readOnly = true)
    public UserProfileResponse getProfile(UUID userId) {
        log.debug("Fetching profile for user: {}", userId);
        SecurityContextHelper.verifyOwnershipOrRole(userId, UserRole.ADMIN, UserRole.OPERATOR);
        User user = findUserById(userId);
        return UserProfileResponse.from(user);
    }

    @Transactional
    public UserProfileResponse updateProfile(UUID userId, UpdateUserProfileRequest request) {
        log.info("Updating profile for user: {}", userId);
        SecurityContextHelper.verifyOwnershipOrRole(userId, UserRole.ADMIN);

        User user = findUserById(userId);

        if (request.email() != null && !request.email().isBlank()) {
            String normalizedEmail = request.email().trim().toLowerCase();
            if (userRepository.existsByEmailAndIdNot(normalizedEmail, userId)) {
                throw new DuplicateResourceException("User", "email", normalizedEmail);
            }
        }

        user.updateProfile(
                request.firstName(),
                request.lastName(),
                request.email(),
                request.district(),
                request.organization(),
                request.telegramUsername(),
                request.preferredLanguage()
        );

        User updated = userRepository.save(user);
        securityAuditService.logAction("PROFILE_UPDATED", "USER", userId.toString(), "User updated profile", "SUCCESS", null);
        return UserProfileResponse.from(updated);
    }

    @Transactional
    public void deactivateOwnAccount(UUID userId) {
        log.info("User self-deactivating account: {}", userId);
        SecurityContextHelper.verifyOwnershipOrRole(userId);

        User user = findUserById(userId);

        if (user.getRole() == UserRole.SUPER_ADMIN && userRepository.countByRoleAndStatus(UserRole.SUPER_ADMIN, UserStatus.ACTIVE) <= 1) {
            throw new BusinessRuleException("The last active Super Administrator cannot deactivate their account", "LAST_SUPER_ADMIN_PROTECTED");
        }

        user.deactivate();
        userRepository.save(user);

        eventPublisher.publishEvent(new UserSecurityInvalidationEvent(userId, "Account self-deactivated"));
        securityAuditService.logAction("ACCOUNT_SELF_DEACTIVATED", "USER", userId.toString(), "Self-deactivated account", "SUCCESS", null);
    }

    @Transactional(readOnly = true)
    public UserProfileResponse getUserById(UUID targetUserId) {
        log.debug("Retrieving user by ID: {}", targetUserId);
        SecurityContextHelper.verifyOwnershipOrRole(targetUserId, UserRole.ADMIN, UserRole.OPERATOR);
        User user = findUserById(targetUserId);
        return UserProfileResponse.from(user);
    }

    @Transactional(readOnly = true)
    public PageResponse<UserProfileResponse> getUsers(
            String search,
            UserRole role,
            UserStatus status,
            String district,
            Pageable pageable) {

        if (!SecurityContextHelper.hasAnyRole(UserRole.ADMIN, UserRole.OPERATOR)) {
            throw new UnauthorizedOperationException("Only administrative and operator personnel can query user directory");
        }

        log.debug("Querying users directory with search='{}', role={}, status={}, district={}",
                search, role, status, district);

        Page<User> page = userRepository.findUsersWithFilters(
                search != null && !search.isBlank() ? search.trim() : null,
                role,
                status,
                district != null && !district.isBlank() ? district.trim() : null,
                pageable
        );

        Page<UserProfileResponse> mapped = page.map(UserProfileResponse::from);
        return PageResponse.from(mapped);
    }

    @Transactional
    public UserProfileResponse createUser(AdminCreateUserRequest request) {
        if (!SecurityContextHelper.hasRole(UserRole.ADMIN)) {
            throw new UnauthorizedOperationException("Only platform administrators can provision users");
        }

        if (request.role() == UserRole.SUPER_ADMIN && !SecurityContextHelper.isSuperAdmin()) {
            securityAuditService.logAction("USER_CREATION_DENIED", "USER", null,
                    "Admin attempted to create SUPER_ADMIN without Super Admin privileges", "DENIED", null);
            throw new UnauthorizedOperationException("Only Super Administrators can create Super Administrator accounts");
        }

        String phone = request.phoneNumber().trim();
        if (userRepository.existsByPhoneNumber(phone)) {
            throw new DuplicateResourceException("User", "phoneNumber", phone);
        }

        String email = request.email() != null && !request.email().isBlank()
                ? request.email().trim().toLowerCase()
                : null;
        if (email != null && userRepository.existsByEmail(email)) {
            throw new DuplicateResourceException("User", "email", email);
        }

        String encodedPassword = passwordEncoder.encode(request.password());

        User user = User.builder()
                .firstName(request.firstName().trim())
                .lastName(request.lastName().trim())
                .fullName((request.firstName().trim() + " " + request.lastName().trim()).trim())
                .phoneNumber(phone)
                .email(email)
                .passwordHash(encodedPassword)
                .role(request.role() != null ? request.role() : UserRole.FARMER)
                .status(request.status() != null ? request.status() : UserStatus.ACTIVE)
                .district(request.district() != null ? request.district().trim() : "Yangiariq")
                .organization(request.organization() != null ? request.organization().trim() : null)
                .telegramUsername(request.telegramUsername() != null ? request.telegramUsername().trim() : null)
                .preferredLanguage(request.preferredLanguage() != null ? request.preferredLanguage().trim() : "uz")
                .build();

        User saved = userRepository.save(user);

        securityAuditService.logAction("USER_CREATED", "USER", saved.getId().toString(),
                "Provisioned user account for " + saved.getEmail() + " with role " + saved.getRole(), "SUCCESS", null);

        return UserProfileResponse.from(saved);
    }

    @Transactional
    public UserProfileResponse changeUserStatus(UUID targetUserId, ChangeUserStatusRequest request) {
        if (!SecurityContextHelper.hasRole(UserRole.ADMIN)) {
            throw new UnauthorizedOperationException("Only platform administrators can change user status");
        }

        User user = findUserById(targetUserId);

        if (user.getRole() == UserRole.SUPER_ADMIN && !SecurityContextHelper.isSuperAdmin()) {
            securityAuditService.logAction("USER_STATUS_CHANGE_DENIED", "USER", targetUserId.toString(),
                    "Admin attempted to change status of Super Administrator", "DENIED", null);
            throw new UnauthorizedOperationException("Administrators cannot alter the status of a Super Administrator");
        }

        if (user.getRole() == UserRole.SUPER_ADMIN && request.status() != UserStatus.ACTIVE) {
            long activeSuperAdmins = userRepository.countByRoleAndStatus(UserRole.SUPER_ADMIN, UserStatus.ACTIVE);
            if (activeSuperAdmins <= 1) {
                throw new BusinessRuleException("The last active Super Administrator cannot be deactivated or blocked", "LAST_SUPER_ADMIN_PROTECTED");
            }
        }

        if (SecurityContextHelper.isCurrentUser(targetUserId) && request.status() != UserStatus.ACTIVE) {
            throw new BusinessRuleException("Administrators cannot deactivate or block their own account", "USER_STATUS_CHANGE_INVALID");
        }

        log.info("Admin updating status of user {} to {} (reason: {})",
                targetUserId, request.status(), request.reason());

        user.changeStatus(request.status());
        User updated = userRepository.save(user);

        if (request.status() == UserStatus.BLOCKED || request.status() == UserStatus.INACTIVE) {
            eventPublisher.publishEvent(new UserSecurityInvalidationEvent(
                    targetUserId,
                    "Status updated to " + request.status() + (request.reason() != null ? ": " + request.reason() : "")
            ));
        }

        securityAuditService.logAction("USER_STATUS_CHANGED", "USER", targetUserId.toString(),
                "Status changed to " + request.status() + (request.reason() != null ? " (" + request.reason() + ")" : ""), "SUCCESS", null);

        return UserProfileResponse.from(updated);
    }

    @Transactional
    public UserProfileResponse changeUserRole(UUID targetUserId, ChangeUserRoleRequest request) {
        if (!SecurityContextHelper.hasRole(UserRole.ADMIN)) {
            throw new UnauthorizedOperationException("Only platform administrators can change user roles");
        }

        User user = findUserById(targetUserId);

        if (request.role() == UserRole.SUPER_ADMIN && !SecurityContextHelper.isSuperAdmin()) {
            securityAuditService.logAction("USER_ROLE_CHANGE_DENIED", "USER", targetUserId.toString(),
                    "Admin attempted to promote user to SUPER_ADMIN", "DENIED", null);
            throw new UnauthorizedOperationException("Only Super Administrators can assign the Super Administrator role");
        }

        if (user.getRole() == UserRole.SUPER_ADMIN && !SecurityContextHelper.isSuperAdmin()) {
            securityAuditService.logAction("USER_ROLE_CHANGE_DENIED", "USER", targetUserId.toString(),
                    "Admin attempted to alter role of a Super Administrator", "DENIED", null);
            throw new UnauthorizedOperationException("Administrators cannot alter the role of a Super Administrator");
        }

        if (user.getRole() == UserRole.SUPER_ADMIN && request.role() != UserRole.SUPER_ADMIN) {
            long activeSuperAdmins = userRepository.countByRoleAndStatus(UserRole.SUPER_ADMIN, UserStatus.ACTIVE);
            if (activeSuperAdmins <= 1) {
                throw new BusinessRuleException("The last active Super Administrator cannot be demoted", "LAST_SUPER_ADMIN_PROTECTED");
            }
        }

        if (SecurityContextHelper.isCurrentUser(targetUserId) && request.role() != user.getRole()) {
            throw new BusinessRuleException("Administrators cannot modify their own role", "USER_ROLE_CHANGE_INVALID");
        }

        log.info("Admin updating role of user {} from {} to {}", targetUserId, user.getRole(), request.role());

        user.changeRole(request.role());
        User updated = userRepository.save(user);

        eventPublisher.publishEvent(new UserSecurityInvalidationEvent(targetUserId, "User role changed to " + request.role()));
        securityAuditService.logAction("USER_ROLE_CHANGED", "USER", targetUserId.toString(),
                "Role changed from " + user.getRole() + " to " + request.role(), "SUCCESS", null);

        return UserProfileResponse.from(updated);
    }

    @Transactional
    public UserProfileResponse adminUpdateUser(UUID targetUserId, AdminUpdateUserRequest request) {
        if (!SecurityContextHelper.hasRole(UserRole.ADMIN)) {
            throw new UnauthorizedOperationException("Only platform administrators can update user profiles directly");
        }

        User user = findUserById(targetUserId);

        if (user.getRole() == UserRole.SUPER_ADMIN && !SecurityContextHelper.isSuperAdmin()) {
            securityAuditService.logAction("USER_UPDATE_DENIED", "USER", targetUserId.toString(),
                    "Admin attempted to update Super Administrator", "DENIED", null);
            throw new UnauthorizedOperationException("Administrators cannot modify Super Administrator accounts");
        }

        if (request.phoneNumber() != null && !request.phoneNumber().isBlank()) {
            String cleanPhone = request.phoneNumber().trim();
            if (userRepository.existsByPhoneNumberAndIdNot(cleanPhone, targetUserId)) {
                throw new DuplicateResourceException("User", "phoneNumber", cleanPhone);
            }
            user.setPhoneNumber(cleanPhone);
        }

        if (request.email() != null && !request.email().isBlank()) {
            String normalizedEmail = request.email().trim().toLowerCase();
            if (userRepository.existsByEmailAndIdNot(normalizedEmail, targetUserId)) {
                throw new DuplicateResourceException("User", "email", normalizedEmail);
            }
        }

        user.updateProfile(
                request.firstName(),
                request.lastName(),
                request.email(),
                request.district(),
                request.organization(),
                request.telegramUsername(),
                request.preferredLanguage()
        );

        if (request.role() != null && request.role() != user.getRole()) {
            if (request.role() == UserRole.SUPER_ADMIN && !SecurityContextHelper.isSuperAdmin()) {
                throw new UnauthorizedOperationException("Only Super Administrators can assign the Super Administrator role");
            }
            if (user.getRole() == UserRole.SUPER_ADMIN && userRepository.countByRoleAndStatus(UserRole.SUPER_ADMIN, UserStatus.ACTIVE) <= 1) {
                throw new BusinessRuleException("The last active Super Administrator cannot be demoted", "LAST_SUPER_ADMIN_PROTECTED");
            }
            if (SecurityContextHelper.isCurrentUser(targetUserId)) {
                throw new BusinessRuleException("Administrators cannot modify their own role", "USER_ROLE_CHANGE_INVALID");
            }
            user.changeRole(request.role());
            eventPublisher.publishEvent(new UserSecurityInvalidationEvent(targetUserId, "Role changed to " + request.role()));
        }

        if (request.status() != null && request.status() != user.getStatus()) {
            if (user.getRole() == UserRole.SUPER_ADMIN && request.status() != UserStatus.ACTIVE) {
                if (userRepository.countByRoleAndStatus(UserRole.SUPER_ADMIN, UserStatus.ACTIVE) <= 1) {
                    throw new BusinessRuleException("The last active Super Administrator cannot be deactivated or blocked", "LAST_SUPER_ADMIN_PROTECTED");
                }
            }
            if (SecurityContextHelper.isCurrentUser(targetUserId) && request.status() != UserStatus.ACTIVE) {
                throw new BusinessRuleException("Administrators cannot deactivate or block their own account", "USER_STATUS_CHANGE_INVALID");
            }
            user.changeStatus(request.status());
            if (request.status() == UserStatus.BLOCKED || request.status() == UserStatus.INACTIVE) {
                eventPublisher.publishEvent(new UserSecurityInvalidationEvent(targetUserId, "Account status changed to " + request.status() + " by admin"));
            }
        }

        User updated = userRepository.save(user);
        securityAuditService.logAction("USER_UPDATED", "USER", targetUserId.toString(), "Admin updated user metadata", "SUCCESS", null);

        return UserProfileResponse.from(updated);
    }

    @Transactional
    public void deleteUser(UUID targetUserId) {
        if (!SecurityContextHelper.hasRole(UserRole.ADMIN)) {
            throw new UnauthorizedOperationException("Only platform administrators can delete accounts");
        }

        if (SecurityContextHelper.isCurrentUser(targetUserId)) {
            throw new BusinessRuleException("Administrators cannot delete their own account", "USER_DELETION_INVALID");
        }

        User user = findUserById(targetUserId);

        if (user.getRole() == UserRole.SUPER_ADMIN && !SecurityContextHelper.isSuperAdmin()) {
            securityAuditService.logAction("USER_DELETION_DENIED", "USER", targetUserId.toString(),
                    "Admin attempted to delete Super Administrator", "DENIED", null);
            throw new UnauthorizedOperationException("Administrators cannot delete a Super Administrator account");
        }

        if (user.getRole() == UserRole.SUPER_ADMIN && userRepository.countByRoleAndStatus(UserRole.SUPER_ADMIN, UserStatus.ACTIVE) <= 1) {
            throw new BusinessRuleException("The last active Super Administrator cannot be deleted", "LAST_SUPER_ADMIN_PROTECTED");
        }

        log.warn("Admin soft-deleting and blocking account for user: {}", targetUserId);
        user.block();
        userRepository.save(user);

        eventPublisher.publishEvent(new UserSecurityInvalidationEvent(targetUserId, "Account blocked and flagged as deleted by admin"));
        securityAuditService.logAction("USER_DELETED", "USER", targetUserId.toString(), "Soft-deleted user account", "SUCCESS", null);
    }

    @Transactional(readOnly = true)
    public UserSummaryStatsResponse getUserStats() {
        if (!SecurityContextHelper.hasAnyRole(UserRole.ADMIN, UserRole.OPERATOR)) {
            throw new UnauthorizedOperationException("Only administrative and operator personnel can access user statistics");
        }

        long total = userRepository.count();
        long active = userRepository.countByStatus(UserStatus.ACTIVE);
        long inactive = userRepository.countByStatus(UserStatus.INACTIVE);
        long blocked = userRepository.countByStatus(UserStatus.BLOCKED);

        long farmers = userRepository.countByRole(UserRole.FARMER);
        long operators = userRepository.countByRole(UserRole.OPERATOR);
        long admins = userRepository.countByRole(UserRole.ADMIN);
        long superAdmins = userRepository.countByRole(UserRole.SUPER_ADMIN);

        return UserSummaryStatsResponse.builder()
                .totalUsers(total)
                .activeUsers(active)
                .inactiveUsers(inactive)
                .blockedUsers(blocked)
                .farmersCount(farmers)
                .operatorsCount(operators)
                .adminsCount(admins)
                .superAdminsCount(superAdmins)
                .build();
    }

    @Transactional(readOnly = true)
    public User findUserById(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
    }
}
