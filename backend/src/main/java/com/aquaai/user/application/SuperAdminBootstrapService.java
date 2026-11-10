package com.aquaai.user.application;

import com.aquaai.admin.application.service.SecurityAuditService;
import com.aquaai.shared.security.SuperAdminBootstrapProperties;
import com.aquaai.shared.security.UserRole;
import com.aquaai.user.domain.User;
import com.aquaai.user.domain.UserRepository;
import com.aquaai.user.domain.UserStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service responsible for controlled initial provisioning (bootstrap) of the root SUPER_ADMIN.
 * 
 * Invariants:
 * 1. Safe first-administrator bootstrap only.
 * 2. Never creates default accounts if at least one active SUPER_ADMIN already exists.
 * 3. Permanently disabled once a SUPER_ADMIN is active.
 * 4. Never logs or prints plaintext passwords.
 * 5. Requires strong password complexity (min 12 chars, upper, lower, digit, symbol).
 * 6. Emits immutable security audit log on successful bootstrap.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SuperAdminBootstrapService {

    private static final List<String> FORBIDDEN_PASSWORDS = List.of(
            "admin123!",
            "admin1234!",
            "password123!",
            "superadmin123!",
            "changeme123!"
    );

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final SecurityAuditService securityAuditService;
    private final SuperAdminBootstrapProperties properties;

    public enum BootstrapResult {
        ALREADY_INITIALIZED,
        DISABLED,
        SKIPPED_NO_CREDENTIALS,
        SUCCESS_CREATED,
        SUCCESS_PROMOTED
    }

    /**
     * Executes controlled bootstrap check and initialization.
     * Idempotent and safe to run on every application startup.
     *
     * @return outcome of the bootstrap operation
     */
    @Transactional
    public BootstrapResult bootstrapIfRequired() {
        long activeSuperAdmins = userRepository.countByRoleAndStatus(UserRole.SUPER_ADMIN, UserStatus.ACTIVE);
        if (activeSuperAdmins > 0) {
            log.debug("Super Administrator bootstrap is permanently disabled: {} active Super Administrator(s) already exist.", activeSuperAdmins);
            return BootstrapResult.ALREADY_INITIALIZED;
        }

        if (!properties.isEnabled()) {
            log.info("No active Super Administrator account detected. Controlled bootstrap is currently DISABLED. Set SUPER_ADMIN_BOOTSTRAP_ENABLED=true with credentials to initialize.");
            return BootstrapResult.DISABLED;
        }

        String rawEmail = properties.getEmail();
        String rawPassword = properties.getPassword();

        if (!StringUtils.hasText(rawEmail) || !StringUtils.hasText(rawPassword)) {
            log.warn("Super Administrator bootstrap is enabled, but email or password is missing. Bootstrap skipped.");
            return BootstrapResult.SKIPPED_NO_CREDENTIALS;
        }

        String email = rawEmail.trim().toLowerCase();
        validatePasswordComplexity(rawPassword);

        String phone = StringUtils.hasText(properties.getPhoneNumber())
                ? properties.getPhoneNumber().trim()
                : "+998900000001";

        Optional<User> existingUserOpt = userRepository.findByEmail(email);

        if (existingUserOpt.isPresent()) {
            User existing = existingUserOpt.get();
            log.info("Elevating existing account ({}) to root SUPER_ADMIN via controlled bootstrap...", maskEmail(email));
            existing.setRole(UserRole.SUPER_ADMIN);
            existing.setStatus(UserStatus.ACTIVE);
            existing.setPasswordHash(passwordEncoder.encode(rawPassword));
            userRepository.save(existing);

            securityAuditService.logAction(
                    "USER_BOOTSTRAP_ELEVATION",
                    "USER",
                    existing.getId().toString(),
                    "Existing user account elevated to root SUPER_ADMIN via controlled bootstrap",
                    "SUCCESS",
                    "127.0.0.1"
            );

            log.info("Super Administrator bootstrap SUCCESS: Account ({}) elevated to SUPER_ADMIN. Bootstrap is now permanently disabled.", maskEmail(email));
            return BootstrapResult.SUCCESS_PROMOTED;
        } else {
            if (userRepository.existsByPhoneNumber(phone)) {
                phone = "+99890" + (Math.abs(email.hashCode()) % 9000000 + 1000000);
            }

            log.info("Provisioning initial root SUPER_ADMIN account ({}) via controlled bootstrap...", maskEmail(email));
            User newSuperAdmin = User.builder()
                    .email(email)
                    .phoneNumber(phone)
                    .firstName(StringUtils.hasText(properties.getFirstName()) ? properties.getFirstName().trim() : "Super")
                    .lastName(StringUtils.hasText(properties.getLastName()) ? properties.getLastName().trim() : "Administrator")
                    .fullName((StringUtils.hasText(properties.getFirstName()) ? properties.getFirstName().trim() : "Super")
                            + " " + (StringUtils.hasText(properties.getLastName()) ? properties.getLastName().trim() : "Administrator"))
                    .passwordHash(passwordEncoder.encode(rawPassword))
                    .role(UserRole.SUPER_ADMIN)
                    .status(UserStatus.ACTIVE)
                    .district("Yangiariq")
                    .build();

            User saved = userRepository.save(newSuperAdmin);

            securityAuditService.logAction(
                    "USER_BOOTSTRAP_INITIALIZATION",
                    "USER",
                    saved.getId().toString(),
                    "Initial root Super Administrator account provisioned via controlled bootstrap",
                    "SUCCESS",
                    "127.0.0.1"
            );

            log.info("Super Administrator bootstrap SUCCESS: Root SUPER_ADMIN account ({}) created. Bootstrap is now permanently disabled.", maskEmail(email));
            return BootstrapResult.SUCCESS_CREATED;
        }
    }

    private void validatePasswordComplexity(String password) {
        if (password.length() < 12) {
            throw new IllegalArgumentException("Super Administrator bootstrap rejected: password length must be at least 12 characters.");
        }

        boolean hasUpper = password.chars().anyMatch(Character::isUpperCase);
        boolean hasLower = password.chars().anyMatch(Character::isLowerCase);
        boolean hasDigit = password.chars().anyMatch(Character::isDigit);
        boolean hasSpecial = password.chars().anyMatch(ch -> "!@#$%^&*()_+-=[]{}|;':\",./<>?`~".indexOf(ch) >= 0);

        if (!hasUpper || !hasLower || !hasDigit || !hasSpecial) {
            throw new IllegalArgumentException("Super Administrator bootstrap rejected: password must contain at least one uppercase letter, one lowercase letter, one digit, and one special character.");
        }

        if (FORBIDDEN_PASSWORDS.contains(password.toLowerCase())) {
            throw new IllegalArgumentException("Super Administrator bootstrap rejected: common or default passwords are strictly forbidden.");
        }
    }

    private String maskEmail(String email) {
        if (!StringUtils.hasText(email) || !email.contains("@")) {
            return "***";
        }
        int atIndex = email.indexOf('@');
        String local = email.substring(0, atIndex);
        String domain = email.substring(atIndex);
        if (local.length() <= 2) {
            return local.charAt(0) + "***" + domain;
        }
        return local.charAt(0) + "***" + local.charAt(local.length() - 1) + domain;
    }
}
