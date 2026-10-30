package com.aquaai.auth.application;

import com.aquaai.auth.api.dto.AuthResponse;
import com.aquaai.auth.api.dto.ChangePasswordRequest;
import com.aquaai.auth.api.dto.LoginRequest;
import com.aquaai.auth.api.dto.PasswordResetConfirmRequest;
import com.aquaai.auth.api.dto.PasswordResetRequest;
import com.aquaai.auth.api.dto.RefreshTokenRequest;
import com.aquaai.auth.api.dto.RegisterRequest;
import com.aquaai.auth.domain.PasswordResetNotificationProvider;
import com.aquaai.auth.domain.PasswordResetToken;
import com.aquaai.auth.domain.PasswordResetTokenRepository;
import com.aquaai.auth.domain.RefreshTokenSession;
import com.aquaai.auth.domain.RefreshTokenSessionRepository;
import com.aquaai.auth.domain.TokenHashUtils;
import com.aquaai.shared.domain.BusinessRuleException;
import com.aquaai.shared.domain.DuplicateResourceException;
import com.aquaai.shared.domain.UnauthorizedOperationException;
import com.aquaai.shared.security.JwtProperties;
import com.aquaai.shared.security.JwtTokenProvider;
import com.aquaai.shared.security.SecurityContextHelper;
import com.aquaai.shared.security.UserRole;
import com.aquaai.user.api.dto.UserProfileResponse;
import com.aquaai.user.domain.User;
import com.aquaai.user.domain.UserRepository;
import com.aquaai.user.domain.UserStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private static final Duration PASSWORD_RESET_VALIDITY = Duration.ofMinutes(15);

    private final UserRepository userRepository;
    private final RefreshTokenSessionRepository refreshTokenSessionRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordResetNotificationProvider passwordResetNotificationProvider;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final JwtProperties jwtProperties;

    @Transactional
    public AuthResponse register(RegisterRequest request, String deviceInfo, String ipAddress) {
        String normalizedPhone = normalizePhone(request.phoneNumber());
        String normalizedEmail = request.email() != null && !request.email().isBlank()
                ? request.email().trim().toLowerCase()
                : null;

        log.info("Processing registration for phone: {}", maskIdentifier(normalizedPhone));

        if (userRepository.existsByPhoneNumber(normalizedPhone)) {
            throw new DuplicateResourceException("User", "phoneNumber", normalizedPhone);
        }

        if (normalizedEmail != null && userRepository.existsByEmail(normalizedEmail)) {
            throw new DuplicateResourceException("User", "email", normalizedEmail);
        }

        User user = User.builder()
                .firstName(request.firstName().trim())
                .lastName(request.lastName().trim())
                .fullName((request.firstName().trim() + " " + request.lastName().trim()).trim())
                .phoneNumber(normalizedPhone)
                .email(normalizedEmail)
                .passwordHash(passwordEncoder.encode(request.password()))
                .role(UserRole.FARMER) // Public registration strictly assigns FARMER
                .status(UserStatus.ACTIVE)
                .district(request.district() != null ? request.district().trim() : "Yangiariq")
                .build();

        User savedUser = userRepository.save(user);

        return createAuthResponseForUser(savedUser, deviceInfo, ipAddress);
    }

    @Transactional
    public AuthResponse login(LoginRequest request, String deviceInfo, String ipAddress) {
        String rawIdentifier = request.identifier().trim();
        log.info("Attempting login for identifier: {}", maskIdentifier(rawIdentifier));

        User user = userRepository.findByIdentifier(rawIdentifier)
                .orElseThrow(() -> new BadCredentialsException("Invalid username or password"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            log.warn("Password mismatch for identifier: {}", maskIdentifier(rawIdentifier));
            throw new BadCredentialsException("Invalid username or password");
        }

        if (user.getStatus() == UserStatus.BLOCKED) {
            log.warn("Login rejected: user {} is BLOCKED", user.getId());
            throw new BusinessRuleException("Your account is blocked. Please contact the district administrator.", "ACCOUNT_BLOCKED");
        }

        if (user.getStatus() != UserStatus.ACTIVE) {
            log.warn("Login rejected: user {} is not ACTIVE (status={})", user.getId(), user.getStatus());
            throw new BusinessRuleException("Your account is currently inactive. Please contact support.", "ACCOUNT_INACTIVE");
        }

        return createAuthResponseForUser(user, deviceInfo, ipAddress);
    }

    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request, String deviceInfo, String ipAddress) {
        String rawToken = request.refreshToken().trim();
        String tokenHash = TokenHashUtils.hashToken(rawToken);

        RefreshTokenSession session = refreshTokenSessionRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new UnauthorizedOperationException("Invalid or unrecognized refresh token", "INVALID_REFRESH_TOKEN"));

        // SECURITY EVENT: Token reuse detection
        if (session.isRevoked()) {
            log.warn("SECURITY ALERT: Compromised refresh token reuse detected for user {} (session: {}). Revoking all active sessions!",
                    session.getUserId(), session.getId());
            refreshTokenSessionRepository.revokeAllActiveSessionsForUser(session.getUserId());
            throw new UnauthorizedOperationException("Security alert: Revoked refresh token reuse detected. All active sessions have been invalidated.", "TOKEN_REUSE_DETECTED");
        }

        // Expiration verification
        if (session.isExpired()) {
            session.revoke();
            refreshTokenSessionRepository.save(session);
            log.info("Refresh token expired for user {}", session.getUserId());
            throw new UnauthorizedOperationException("Refresh token has expired. Please log in again.", "REFRESH_TOKEN_EXPIRED");
        }

        User user = userRepository.findById(session.getUserId())
                .orElseThrow(() -> new UnauthorizedOperationException("User associated with session not found", "USER_NOT_FOUND"));

        if (!user.isAllowedToAuthenticate()) {
            session.revoke();
            refreshTokenSessionRepository.save(session);
            throw new BusinessRuleException("User account is no longer active", "ACCOUNT_NOT_ACTIVE");
        }

        // Issue new refresh token & session (Rotation)
        String newRawRefreshToken = generateSecureRefreshTokenValue();
        String newTokenHash = TokenHashUtils.hashToken(newRawRefreshToken);
        Instant newExpiresAt = Instant.now().plusMillis(jwtProperties.getRefreshTokenExpirationMs());

        RefreshTokenSession newSession = RefreshTokenSession.builder()
                .userId(user.getId())
                .tokenHash(newTokenHash)
                .expiresAt(newExpiresAt)
                .deviceInfo(deviceInfo != null ? deviceInfo : session.getDeviceInfo())
                .ipAddress(ipAddress != null ? ipAddress : session.getIpAddress())
                .build();

        RefreshTokenSession savedNewSession = refreshTokenSessionRepository.save(newSession);

        // Revoke old session and link to replacement
        session.replaceWith(savedNewSession.getId());
        refreshTokenSessionRepository.save(session);

        String newAccessToken = tokenProvider.generateAccessToken(user.getId(), user.getPhoneNumber(), user.getRole());

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRawRefreshToken)
                .tokenType("Bearer")
                .expiresInSeconds(jwtProperties.getAccessTokenExpirationMs() / 1000)
                .user(UserProfileResponse.from(user))
                .build();
    }

    @Transactional
    public void logout(String rawRefreshToken) {
        if (rawRefreshToken != null && !rawRefreshToken.isBlank()) {
            try {
                String tokenHash = TokenHashUtils.hashToken(rawRefreshToken.trim());
                refreshTokenSessionRepository.findByTokenHash(tokenHash).ifPresent(session -> {
                    if (session.isValid()) {
                        session.revoke();
                        refreshTokenSessionRepository.save(session);
                        log.info("Successfully revoked refresh session {} during logout", session.getId());
                    }
                });
            } catch (Exception ex) {
                log.warn("Non-fatal error encountered during logout token revocation: {}", ex.getMessage());
            }
        }
    }

    @Transactional
    public void changePassword(UUID userId, ChangePasswordRequest request) {
        SecurityContextHelper.verifyOwnershipOrRole(userId, UserRole.ADMIN);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UnauthorizedOperationException("User not found"));

        if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            throw new BadCredentialsException("Current password does not match");
        }

        if (passwordEncoder.matches(request.newPassword(), user.getPasswordHash())) {
            throw new BusinessRuleException("New password cannot be identical to current password", "PASSWORD_UNCHANGED");
        }

        user.changePassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);

        // Security policy: Invalidate all active refresh sessions to enforce re-login across devices
        refreshTokenSessionRepository.revokeAllActiveSessionsForUser(userId);
        log.info("Password changed successfully for user {}. All other active sessions revoked.", userId);
    }

    @Transactional
    public void requestPasswordReset(PasswordResetRequest request) {
        String identifier = request.identifier().trim();
        log.info("Password reset requested for identifier: {}", maskIdentifier(identifier));

        userRepository.findByIdentifier(identifier).ifPresent(user -> {
            if (user.getStatus() == UserStatus.BLOCKED) {
                log.warn("Password reset skipped: user {} is BLOCKED", user.getId());
                return;
            }

            // Invalidate any previously active unused reset tokens
            passwordResetTokenRepository.invalidateAllActiveForUser(user.getId());

            String rawToken = UUID.randomUUID().toString();
            String tokenHash = TokenHashUtils.hashToken(rawToken);
            Instant expiresAt = Instant.now().plus(PASSWORD_RESET_VALIDITY);

            PasswordResetToken resetToken = PasswordResetToken.builder()
                    .userId(user.getId())
                    .tokenHash(tokenHash)
                    .expiresAt(expiresAt)
                    .build();

            passwordResetTokenRepository.save(resetToken);

            String recipient = user.getEmail() != null && !user.getEmail().isBlank()
                    ? user.getEmail()
                    : user.getPhoneNumber();

            passwordResetNotificationProvider.sendPasswordResetNotification(recipient, rawToken, expiresAt);
        });
        // Non-enumerating: always completes cleanly regardless of whether user was found
    }

    @Transactional
    public void confirmPasswordReset(PasswordResetConfirmRequest request) {
        String tokenHash = TokenHashUtils.hashToken(request.token().trim());

        PasswordResetToken resetToken = passwordResetTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new BusinessRuleException("Invalid or unrecognized password reset token", "INVALID_RESET_TOKEN"));

        if (resetToken.isUsed()) {
            throw new BusinessRuleException("This password reset token has already been used", "RESET_TOKEN_ALREADY_USED");
        }

        if (resetToken.isExpired()) {
            throw new BusinessRuleException("This password reset token has expired", "RESET_TOKEN_EXPIRED");
        }

        User user = userRepository.findById(resetToken.getUserId())
                .orElseThrow(() -> new BusinessRuleException("User associated with reset token not found", "USER_NOT_FOUND"));

        if (user.getStatus() == UserStatus.BLOCKED) {
            throw new BusinessRuleException("Your account is blocked. Please contact administrator.", "ACCOUNT_BLOCKED");
        }

        user.changePassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);

        resetToken.markUsed();
        passwordResetTokenRepository.save(resetToken);

        // Security measure: Revoke all active sessions upon password reset
        refreshTokenSessionRepository.revokeAllActiveSessionsForUser(user.getId());
        log.info("Password reset confirmed for user {}. All active sessions invalidated.", user.getId());
    }

    private AuthResponse createAuthResponseForUser(User user, String deviceInfo, String ipAddress) {
        String accessToken = tokenProvider.generateAccessToken(user.getId(), user.getPhoneNumber(), user.getRole());
        String rawRefreshToken = generateSecureRefreshTokenValue();
        String tokenHash = TokenHashUtils.hashToken(rawRefreshToken);
        Instant expiresAt = Instant.now().plusMillis(jwtProperties.getRefreshTokenExpirationMs());

        RefreshTokenSession session = RefreshTokenSession.builder()
                .userId(user.getId())
                .tokenHash(tokenHash)
                .expiresAt(expiresAt)
                .deviceInfo(deviceInfo)
                .ipAddress(ipAddress)
                .build();

        refreshTokenSessionRepository.save(session);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(rawRefreshToken)
                .tokenType("Bearer")
                .expiresInSeconds(jwtProperties.getAccessTokenExpirationMs() / 1000)
                .user(UserProfileResponse.from(user))
                .build();
    }

    private String generateSecureRefreshTokenValue() {
        return UUID.randomUUID().toString() + "-" + UUID.randomUUID().toString();
    }

    private String normalizePhone(String rawPhone) {
        if (rawPhone == null) return null;
        return rawPhone.trim().replaceAll("[\\s-]", "");
    }

    private String maskIdentifier(String identifier) {
        if (identifier == null || identifier.length() < 4) return "***";
        return identifier.substring(0, 2) + "***" + identifier.substring(identifier.length() - 2);
    }
}
