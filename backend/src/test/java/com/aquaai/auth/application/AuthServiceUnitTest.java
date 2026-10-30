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
import com.aquaai.shared.security.UserPrincipal;
import com.aquaai.shared.security.UserRole;
import com.aquaai.user.domain.User;
import com.aquaai.user.domain.UserRepository;
import com.aquaai.user.domain.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceUnitTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RefreshTokenSessionRepository refreshTokenSessionRepository;

    @Mock
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Mock
    private PasswordResetNotificationProvider passwordResetNotificationProvider;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider tokenProvider;

    private JwtProperties jwtProperties;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        jwtProperties = new JwtProperties();
        jwtProperties.setAccessTokenExpirationMs(3600000);
        jwtProperties.setRefreshTokenExpirationMs(604800000);

        authService = new AuthService(
                userRepository,
                refreshTokenSessionRepository,
                passwordResetTokenRepository,
                passwordResetNotificationProvider,
                passwordEncoder,
                tokenProvider,
                jwtProperties
        );
    }

    @Test
    @DisplayName("Registration should normalize phone and assign FARMER role and ACTIVE status")
    void testRegisterSuccess() {
        RegisterRequest request = RegisterRequest.builder()
                .firstName("Alisher")
                .lastName("Navoiy")
                .phoneNumber(" +998 90 123-45-67 ")
                .email("Alisher@Aqua.uz")
                .password("securePassword123")
                .district("Yangiariq")
                .build();

        when(userRepository.existsByPhoneNumber("+998901234567")).thenReturn(false);
        when(userRepository.existsByEmail("alisher@aqua.uz")).thenReturn(false);
        when(passwordEncoder.encode("securePassword123")).thenReturn("hashedPass");
        when(tokenProvider.generateAccessToken(any(), eq("+998901234567"), eq(UserRole.FARMER))).thenReturn("test-access-token");

        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(UUID.randomUUID());
            return u;
        });

        AuthResponse response = authService.register(request, "Mozilla/5.0", "127.0.0.1");

        assertThat(response.accessToken()).isEqualTo("test-access-token");
        assertThat(response.refreshToken()).isNotBlank();
        assertThat(response.user().role()).isEqualTo(UserRole.FARMER);
        assertThat(response.user().status()).isEqualTo(UserStatus.ACTIVE);
        assertThat(response.user().phoneNumber()).isEqualTo("+998901234567");
        assertThat(response.user().email()).isEqualTo("alisher@aqua.uz");

        verify(refreshTokenSessionRepository).save(any(RefreshTokenSession.class));
    }

    @Test
    @DisplayName("Registration should reject duplicate phone number")
    void testRegisterDuplicatePhone() {
        RegisterRequest request = RegisterRequest.builder()
                .firstName("Bobur")
                .lastName("Mirzo")
                .phoneNumber("+998901112233")
                .password("password123")
                .build();

        when(userRepository.existsByPhoneNumber("+998901112233")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request, "device", "127.0.0.1"))
                .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    @DisplayName("Login should reject non-existent user with generic BadCredentialsException")
    void testLoginUserNotFound() {
        LoginRequest request = LoginRequest.builder()
                .identifier("+998900000000")
                .password("anyPassword")
                .build();

        when(userRepository.findByIdentifier("+998900000000")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(request, "device", "127.0.0.1"))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Invalid username or password");
    }

    @Test
    @DisplayName("Login should reject password mismatch with generic BadCredentialsException")
    void testLoginPasswordMismatch() {
        User user = User.builder()
                .phoneNumber("+998901234567")
                .passwordHash("hashedSecret")
                .status(UserStatus.ACTIVE)
                .build();
        user.setId(UUID.randomUUID());

        when(userRepository.findByIdentifier("+998901234567")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongPassword", "hashedSecret")).thenReturn(false);

        LoginRequest request = LoginRequest.builder()
                .identifier("+998901234567")
                .password("wrongPassword")
                .build();

        assertThatThrownBy(() -> authService.login(request, "device", "127.0.0.1"))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Invalid username or password");
    }

    @Test
    @DisplayName("Login should reject BLOCKED user with business rule exception")
    void testLoginBlockedUser() {
        User user = User.builder()
                .phoneNumber("+998901234567")
                .passwordHash("hashedSecret")
                .status(UserStatus.BLOCKED)
                .build();
        user.setId(UUID.randomUUID());

        when(userRepository.findByIdentifier("+998901234567")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("validPassword", "hashedSecret")).thenReturn(true);

        LoginRequest request = LoginRequest.builder()
                .identifier("+998901234567")
                .password("validPassword")
                .build();

        assertThatThrownBy(() -> authService.login(request, "device", "127.0.0.1"))
                .isInstanceOf(BusinessRuleException.class)
                .hasFieldOrPropertyWithValue("errorCode", "ACCOUNT_BLOCKED");
    }

    @Test
    @DisplayName("Refresh rotation should succeed, revoke old session and issue new tokens")
    void testRefreshTokenRotationSuccess() {
        UUID userId = UUID.randomUUID();
        User user = User.builder()
                .firstName("Sobir")
                .lastName("Rahimov")
                .phoneNumber("+998909876543")
                .role(UserRole.FARMER)
                .status(UserStatus.ACTIVE)
                .build();
        user.setId(userId);

        String rawToken = "sample-raw-refresh-token-uuid";
        String tokenHash = TokenHashUtils.hashToken(rawToken);

        RefreshTokenSession oldSession = RefreshTokenSession.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .tokenHash(tokenHash)
                .expiresAt(Instant.now().plus(7, ChronoUnit.DAYS))
                .build();

        when(refreshTokenSessionRepository.findByTokenHash(tokenHash)).thenReturn(Optional.of(oldSession));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(tokenProvider.generateAccessToken(eq(userId), eq("+998909876543"), eq(UserRole.FARMER))).thenReturn("new-access-token");
        when(refreshTokenSessionRepository.save(any(RefreshTokenSession.class))).thenAnswer(inv -> {
            RefreshTokenSession s = inv.getArgument(0);
            if (s.getId() == null) {
                s.setId(UUID.randomUUID());
            }
            return s;
        });

        RefreshTokenRequest request = new RefreshTokenRequest(rawToken);
        AuthResponse response = authService.refreshToken(request, "Mobile-App", "10.0.0.1");

        assertThat(response.accessToken()).isEqualTo("new-access-token");
        assertThat(response.refreshToken()).isNotEqualTo(rawToken);
        assertThat(oldSession.isRevoked()).isTrue();
        assertThat(oldSession.getReplacedBySessionId()).isNotNull();
    }

    @Test
    @DisplayName("Refresh token reuse must trigger security alert and revoke all user active sessions")
    void testRefreshTokenReuseDetection() {
        UUID userId = UUID.randomUUID();
        String compromisedToken = "compromised-replayed-token";
        String tokenHash = TokenHashUtils.hashToken(compromisedToken);

        RefreshTokenSession revokedSession = RefreshTokenSession.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .tokenHash(tokenHash)
                .expiresAt(Instant.now().plus(5, ChronoUnit.DAYS))
                .revokedAt(Instant.now().minus(1, ChronoUnit.HOURS)) // Already revoked!
                .build();

        when(refreshTokenSessionRepository.findByTokenHash(tokenHash)).thenReturn(Optional.of(revokedSession));

        RefreshTokenRequest request = new RefreshTokenRequest(compromisedToken);

        assertThatThrownBy(() -> authService.refreshToken(request, "Attacker-Device", "192.168.1.1"))
                .isInstanceOf(UnauthorizedOperationException.class)
                .hasFieldOrPropertyWithValue("errorCode", "TOKEN_REUSE_DETECTED");

        // Verifies that ALL active sessions for this user were revoked to protect the account
        verify(refreshTokenSessionRepository).revokeAllActiveSessionsForUser(userId);
    }

    @Test
    @DisplayName("Password change should verify current password, update hash, and revoke all active sessions")
    void testChangePasswordSuccess() {
        UUID userId = UUID.randomUUID();
        User user = User.builder()
                .phoneNumber("+998901234567")
                .passwordHash("oldHash")
                .build();
        user.setId(userId);

        // Setup security context with current user
        UserPrincipal principal = UserPrincipal.builder().id(userId).role(UserRole.FARMER).enabled(true).build();
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("oldPass123", "oldHash")).thenReturn(true);
        when(passwordEncoder.matches("newPass456", "oldHash")).thenReturn(false);
        when(passwordEncoder.encode("newPass456")).thenReturn("newHashedPassword");

        ChangePasswordRequest request = ChangePasswordRequest.builder()
                .currentPassword("oldPass123")
                .newPassword("newPass456")
                .build();

        authService.changePassword(userId, request);

        assertThat(user.getPasswordHash()).isEqualTo("newHashedPassword");
        verify(userRepository).save(user);
        verify(refreshTokenSessionRepository).revokeAllActiveSessionsForUser(userId);
    }

    @Test
    @DisplayName("Password reset confirmation should update password, mark token used, and revoke sessions")
    void testPasswordResetConfirmation() {
        UUID userId = UUID.randomUUID();
        User user = User.builder()
                .phoneNumber("+998901234567")
                .passwordHash("previousHash")
                .status(UserStatus.ACTIVE)
                .build();
        user.setId(userId);

        String rawToken = "secure-random-reset-uuid";
        String tokenHash = TokenHashUtils.hashToken(rawToken);

        PasswordResetToken resetToken = PasswordResetToken.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .tokenHash(tokenHash)
                .expiresAt(Instant.now().plus(10, ChronoUnit.MINUTES))
                .build();

        when(passwordResetTokenRepository.findByTokenHash(tokenHash)).thenReturn(Optional.of(resetToken));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("newBrandPassword123")).thenReturn("newHashedPass");

        PasswordResetConfirmRequest request = PasswordResetConfirmRequest.builder()
                .token(rawToken)
                .newPassword("newBrandPassword123")
                .build();

        authService.confirmPasswordReset(request);

        assertThat(user.getPasswordHash()).isEqualTo("newHashedPass");
        assertThat(resetToken.isUsed()).isTrue();
        verify(passwordResetTokenRepository).save(resetToken);
        verify(refreshTokenSessionRepository).revokeAllActiveSessionsForUser(userId);
    }
}
