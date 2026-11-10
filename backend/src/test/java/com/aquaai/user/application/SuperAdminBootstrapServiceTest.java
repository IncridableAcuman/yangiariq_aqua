package com.aquaai.user.application;

import com.aquaai.admin.application.service.SecurityAuditService;
import com.aquaai.shared.security.SuperAdminBootstrapProperties;
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
import org.springframework.security.crypto.password.PasswordEncoder;

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
class SuperAdminBootstrapServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private SecurityAuditService securityAuditService;

    private SuperAdminBootstrapProperties properties;
    private SuperAdminBootstrapService bootstrapService;

    private static final String STRONG_PASSWORD = "StrongSuperAdminPass2026!#$";

    @BeforeEach
    void setUp() {
        properties = new SuperAdminBootstrapProperties();
        bootstrapService = new SuperAdminBootstrapService(
                userRepository,
                passwordEncoder,
                securityAuditService,
                properties
        );
    }

    @Test
    @DisplayName("Bootstrap is permanently disabled when an active SUPER_ADMIN already exists")
    void testBootstrapDisabledWhenSuperAdminAlreadyExists() {
        when(userRepository.countByRoleAndStatus(UserRole.SUPER_ADMIN, UserStatus.ACTIVE)).thenReturn(1L);

        SuperAdminBootstrapService.BootstrapResult result = bootstrapService.bootstrapIfRequired();

        assertThat(result).isEqualTo(SuperAdminBootstrapService.BootstrapResult.ALREADY_INITIALIZED);
        verify(userRepository, never()).save(any(User.class));
        verify(securityAuditService, never()).logAction(any(), any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("Bootstrap returns DISABLED when properties.enabled is false and no SUPER_ADMIN exists")
    void testBootstrapReturnsDisabledWhenFlagIsFalse() {
        when(userRepository.countByRoleAndStatus(UserRole.SUPER_ADMIN, UserStatus.ACTIVE)).thenReturn(0L);
        properties.setEnabled(false);

        SuperAdminBootstrapService.BootstrapResult result = bootstrapService.bootstrapIfRequired();

        assertThat(result).isEqualTo(SuperAdminBootstrapService.BootstrapResult.DISABLED);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Bootstrap returns SKIPPED_NO_CREDENTIALS when enabled but credentials missing")
    void testBootstrapSkipsWhenCredentialsMissing() {
        when(userRepository.countByRoleAndStatus(UserRole.SUPER_ADMIN, UserStatus.ACTIVE)).thenReturn(0L);
        properties.setEnabled(true);
        properties.setEmail(null);
        properties.setPassword(null);

        SuperAdminBootstrapService.BootstrapResult result = bootstrapService.bootstrapIfRequired();

        assertThat(result).isEqualTo(SuperAdminBootstrapService.BootstrapResult.SKIPPED_NO_CREDENTIALS);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Bootstrap rejects passwords shorter than 12 characters")
    void testBootstrapRejectsShortPassword() {
        when(userRepository.countByRoleAndStatus(UserRole.SUPER_ADMIN, UserStatus.ACTIVE)).thenReturn(0L);
        properties.setEnabled(true);
        properties.setEmail("admin@aqua.uz");
        properties.setPassword("Short1!");

        assertThatThrownBy(() -> bootstrapService.bootstrapIfRequired())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("must be at least 12 characters");
    }

    @Test
    @DisplayName("Bootstrap rejects passwords lacking required character classes")
    void testBootstrapRejectsPasswordWithoutSpecialChar() {
        when(userRepository.countByRoleAndStatus(UserRole.SUPER_ADMIN, UserStatus.ACTIVE)).thenReturn(0L);
        properties.setEnabled(true);
        properties.setEmail("admin@aqua.uz");
        properties.setPassword("AllLettersNoDigitsNoSymbols");

        assertThatThrownBy(() -> bootstrapService.bootstrapIfRequired())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("must contain at least one uppercase letter, one lowercase letter, one digit, and one special character");
    }

    @Test
    @DisplayName("Bootstrap rejects common or default weak passwords")
    void testBootstrapRejectsForbiddenDefaultPasswords() {
        when(userRepository.countByRoleAndStatus(UserRole.SUPER_ADMIN, UserStatus.ACTIVE)).thenReturn(0L);
        properties.setEnabled(true);
        properties.setEmail("admin@aqua.uz");
        properties.setPassword("Admin123!");

        assertThatThrownBy(() -> bootstrapService.bootstrapIfRequired())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("must be at least 12 characters");
    }

    @Test
    @DisplayName("Bootstrap creates new root SUPER_ADMIN and logs security audit")
    void testBootstrapCreatesRootSuperAdmin() {
        when(userRepository.countByRoleAndStatus(UserRole.SUPER_ADMIN, UserStatus.ACTIVE)).thenReturn(0L);
        when(userRepository.findByEmail("superadmin@aqua.uz")).thenReturn(Optional.empty());
        when(userRepository.existsByPhoneNumber(anyString())).thenReturn(false);
        when(passwordEncoder.encode(STRONG_PASSWORD)).thenReturn("$2a$12$encodedHashForSuperAdmin");

        UUID generatedId = UUID.randomUUID();
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(generatedId);
            return u;
        });

        properties.setEnabled(true);
        properties.setEmail("superadmin@aqua.uz");
        properties.setPassword(STRONG_PASSWORD);
        properties.setPhoneNumber("+998901234567");
        properties.setFirstName("Root");
        properties.setLastName("Administrator");

        SuperAdminBootstrapService.BootstrapResult result = bootstrapService.bootstrapIfRequired();

        assertThat(result).isEqualTo(SuperAdminBootstrapService.BootstrapResult.SUCCESS_CREATED);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User created = captor.getValue();
        assertThat(created.getEmail()).isEqualTo("superadmin@aqua.uz");
        assertThat(created.getRole()).isEqualTo(UserRole.SUPER_ADMIN);
        assertThat(created.getStatus()).isEqualTo(UserStatus.ACTIVE);
        assertThat(created.getPasswordHash()).isEqualTo("$2a$12$encodedHashForSuperAdmin");

        verify(securityAuditService).logAction(
                eq("USER_BOOTSTRAP_INITIALIZATION"),
                eq("USER"),
                eq(generatedId.toString()),
                anyString(),
                eq("SUCCESS"),
                eq("127.0.0.1")
        );
    }

    @Test
    @DisplayName("Bootstrap elevates existing account to SUPER_ADMIN when email matches")
    void testBootstrapElevatesExistingAccount() {
        UUID existingId = UUID.randomUUID();
        User existingUser = User.builder()
                .email("designated@aqua.uz")
                .phoneNumber("+998909998877")
                .role(UserRole.ADMIN)
                .status(UserStatus.ACTIVE)
                .passwordHash("oldHash")
                .build();
        existingUser.setId(existingId);

        when(userRepository.countByRoleAndStatus(UserRole.SUPER_ADMIN, UserStatus.ACTIVE)).thenReturn(0L);
        when(userRepository.findByEmail("designated@aqua.uz")).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.encode(STRONG_PASSWORD)).thenReturn("$2a$12$newSuperHash");
        when(userRepository.save(any(User.class))).thenReturn(existingUser);

        properties.setEnabled(true);
        properties.setEmail("designated@aqua.uz");
        properties.setPassword(STRONG_PASSWORD);

        SuperAdminBootstrapService.BootstrapResult result = bootstrapService.bootstrapIfRequired();

        assertThat(result).isEqualTo(SuperAdminBootstrapService.BootstrapResult.SUCCESS_PROMOTED);
        assertThat(existingUser.getRole()).isEqualTo(UserRole.SUPER_ADMIN);
        assertThat(existingUser.getPasswordHash()).isEqualTo("$2a$12$newSuperHash");

        verify(securityAuditService).logAction(
                eq("USER_BOOTSTRAP_ELEVATION"),
                eq("USER"),
                eq(existingId.toString()),
                anyString(),
                eq("SUCCESS"),
                eq("127.0.0.1")
        );
    }
}
