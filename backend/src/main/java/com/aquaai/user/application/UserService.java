package com.aquaai.user.application;

import com.aquaai.shared.domain.DuplicateResourceException;
import com.aquaai.shared.domain.ResourceNotFoundException;
import com.aquaai.shared.security.SecurityContextHelper;
import com.aquaai.shared.security.UserRole;
import com.aquaai.user.api.dto.UpdateUserProfileRequest;
import com.aquaai.user.api.dto.UserProfileResponse;
import com.aquaai.user.domain.User;
import com.aquaai.user.domain.UserRepository;
import com.aquaai.user.domain.UserStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public UserProfileResponse getProfile(UUID userId) {
        log.debug("Fetching profile for user: {}", userId);
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

        user.updateProfile(request.firstName(), request.lastName(), request.email(), request.district());
        User updated = userRepository.save(user);

        return UserProfileResponse.from(updated);
    }

    @Transactional
    public UserProfileResponse changeUserStatus(UUID targetUserId, UserStatus newStatus) {
        SecurityContextHelper.verifyOwnershipOrRole(null, UserRole.ADMIN);
        log.info("Admin updating status of user {} to {}", targetUserId, newStatus);

        User user = findUserById(targetUserId);
        user.setStatus(newStatus);
        User updated = userRepository.save(user);

        return UserProfileResponse.from(updated);
    }

    @Transactional
    public UserProfileResponse changeUserRole(UUID targetUserId, UserRole newRole) {
        SecurityContextHelper.verifyOwnershipOrRole(null, UserRole.ADMIN);
        log.info("Admin updating role of user {} to {}", targetUserId, newRole);

        User user = findUserById(targetUserId);
        user.setRole(newRole);
        User updated = userRepository.save(user);

        return UserProfileResponse.from(updated);
    }

    @Transactional(readOnly = true)
    public User findUserById(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
    }
}
