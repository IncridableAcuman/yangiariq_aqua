package com.aquaai.user.application;

import com.aquaai.shared.api.PageResponse;
import com.aquaai.shared.domain.BusinessRuleException;
import com.aquaai.shared.domain.DuplicateResourceException;
import com.aquaai.shared.domain.ResourceNotFoundException;
import com.aquaai.shared.domain.UnauthorizedOperationException;
import com.aquaai.shared.security.SecurityContextHelper;
import com.aquaai.shared.security.UserRole;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;

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
        return UserProfileResponse.from(updated);
    }

    @Transactional
    public void deactivateOwnAccount(UUID userId) {
        log.info("User self-deactivating account: {}", userId);
        SecurityContextHelper.verifyOwnershipOrRole(userId);

        User user = findUserById(userId);
        user.deactivate();
        userRepository.save(user);

        eventPublisher.publishEvent(new UserSecurityInvalidationEvent(userId, "Account self-deactivated"));
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
    public UserProfileResponse changeUserStatus(UUID targetUserId, ChangeUserStatusRequest request) {
        if (!SecurityContextHelper.hasRole(UserRole.ADMIN)) {
            throw new UnauthorizedOperationException("Only platform administrators can change user status");
        }

        if (SecurityContextHelper.isCurrentUser(targetUserId) && request.status() != UserStatus.ACTIVE) {
            throw new BusinessRuleException("Administrators cannot deactivate or block their own account", "USER_STATUS_CHANGE_INVALID");
        }

        log.info("Admin updating status of user {} to {} (reason: {})",
                targetUserId, request.status(), request.reason());

        User user = findUserById(targetUserId);
        user.changeStatus(request.status());
        User updated = userRepository.save(user);

        if (request.status() == UserStatus.BLOCKED || request.status() == UserStatus.INACTIVE) {
            eventPublisher.publishEvent(new UserSecurityInvalidationEvent(
                    targetUserId,
                    "Status updated to " + request.status() + (request.reason() != null ? ": " + request.reason() : "")
            ));
        }

        return UserProfileResponse.from(updated);
    }

    @Transactional
    public UserProfileResponse changeUserRole(UUID targetUserId, ChangeUserRoleRequest request) {
        if (!SecurityContextHelper.hasRole(UserRole.ADMIN)) {
            throw new UnauthorizedOperationException("Only platform administrators can change user roles");
        }

        if (SecurityContextHelper.isCurrentUser(targetUserId) && request.role() != UserRole.ADMIN) {
            throw new BusinessRuleException("Administrators cannot revoke their own administrative privileges", "USER_ROLE_CHANGE_INVALID");
        }

        log.info("Admin updating role of user {} to {}", targetUserId, request.role());

        User user = findUserById(targetUserId);
        user.changeRole(request.role());
        User updated = userRepository.save(user);

        return UserProfileResponse.from(updated);
    }

    @Transactional
    public UserProfileResponse adminUpdateUser(UUID targetUserId, AdminUpdateUserRequest request) {
        if (!SecurityContextHelper.hasRole(UserRole.ADMIN)) {
            throw new UnauthorizedOperationException("Only platform administrators can update user profiles directly");
        }

        User user = findUserById(targetUserId);

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

        if (request.role() != null) {
            if (SecurityContextHelper.isCurrentUser(targetUserId) && request.role() != UserRole.ADMIN) {
                throw new BusinessRuleException("Administrators cannot revoke their own administrative privileges", "USER_ROLE_CHANGE_INVALID");
            }
            user.changeRole(request.role());
        }

        if (request.status() != null) {
            if (SecurityContextHelper.isCurrentUser(targetUserId) && request.status() != UserStatus.ACTIVE) {
                throw new BusinessRuleException("Administrators cannot deactivate or block their own account", "USER_STATUS_CHANGE_INVALID");
            }
            user.changeStatus(request.status());
            if (request.status() == UserStatus.BLOCKED || request.status() == UserStatus.INACTIVE) {
                eventPublisher.publishEvent(new UserSecurityInvalidationEvent(targetUserId, "Account status changed to " + request.status() + " by admin"));
            }
        }

        User updated = userRepository.save(user);
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

        log.warn("Admin soft-deleting and blocking account for user: {}", targetUserId);
        User user = findUserById(targetUserId);
        user.block();
        userRepository.save(user);

        eventPublisher.publishEvent(new UserSecurityInvalidationEvent(targetUserId, "Account blocked and flagged as deleted by admin"));
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

        return UserSummaryStatsResponse.builder()
                .totalUsers(total)
                .activeUsers(active)
                .inactiveUsers(inactive)
                .blockedUsers(blocked)
                .farmersCount(farmers)
                .operatorsCount(operators)
                .adminsCount(admins)
                .build();
    }

    @Transactional(readOnly = true)
    public User findUserById(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
    }
}
