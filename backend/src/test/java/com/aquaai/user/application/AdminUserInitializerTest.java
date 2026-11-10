package com.aquaai.user.application;

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
import org.springframework.mock.env.MockEnvironment;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminUserInitializerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private MockEnvironment environment;
    private AdminUserInitializer initializer;

    @BeforeEach
    void setUp() {
        environment = new MockEnvironment();
        initializer = new AdminUserInitializer(userRepository, passwordEncoder, environment);
    }

    @Test
    @DisplayName("Elevates existing non-admin user to ADMIN role")
    void testElevateExistingNonAdminUser() {
        User existingUser = User.builder()
                .email("abdusharipovizzat03@gmail.com")
                .phoneNumber("+998901112233")
                .role(UserRole.FARMER)
                .status(UserStatus.ACTIVE)
                .build();
        existingUser.setId(UUID.randomUUID());

        when(userRepository.findByEmail("abdusharipovizzat03@gmail.com")).thenReturn(Optional.of(existingUser));
        when(userRepository.findByEmail("izzatbek.engineer@gmail.com")).thenReturn(Optional.empty());
        when(userRepository.existsByPhoneNumber(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashedPass");

        initializer.run(null);

        assertThat(existingUser.getRole()).isEqualTo(UserRole.ADMIN);
        verify(userRepository).save(existingUser);
    }

    @Test
    @DisplayName("Preserves SUPER_ADMIN role if designated user already has it")
    void testPreserveSuperAdminRole() {
        User existingSuperAdmin = User.builder()
                .email("abdusharipovizzat03@gmail.com")
                .phoneNumber("+998901112233")
                .role(UserRole.SUPER_ADMIN)
                .status(UserStatus.ACTIVE)
                .build();
        existingSuperAdmin.setId(UUID.randomUUID());

        User existingAdmin = User.builder()
                .email("izzatbek.engineer@gmail.com")
                .phoneNumber("+998902223344")
                .role(UserRole.ADMIN)
                .status(UserStatus.ACTIVE)
                .build();
        existingAdmin.setId(UUID.randomUUID());

        when(userRepository.findByEmail("abdusharipovizzat03@gmail.com")).thenReturn(Optional.of(existingSuperAdmin));
        when(userRepository.findByEmail("izzatbek.engineer@gmail.com")).thenReturn(Optional.of(existingAdmin));

        initializer.run(null);

        assertThat(existingSuperAdmin.getRole()).isEqualTo(UserRole.SUPER_ADMIN);
        assertThat(existingAdmin.getRole()).isEqualTo(UserRole.ADMIN);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Provisions baseline admin accounts when neither exists in non-prod environment")
    void testProvisionsBaselineAdminAccountsWhenNeitherExists() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(userRepository.existsByPhoneNumber(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashedPass");

        initializer.run(null);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository, times(2)).save(captor.capture());

        var savedUsers = captor.getAllValues();
        assertThat(savedUsers).hasSize(2);

        User first = savedUsers.get(0);
        assertThat(first.getEmail()).isEqualTo("abdusharipovizzat03@gmail.com");
        assertThat(first.getRole()).isEqualTo(UserRole.ADMIN);
        assertThat(first.getStatus()).isEqualTo(UserStatus.ACTIVE);

        User second = savedUsers.get(1);
        assertThat(second.getEmail()).isEqualTo("izzatbek.engineer@gmail.com");
        assertThat(second.getRole()).isEqualTo(UserRole.ADMIN);
        assertThat(second.getStatus()).isEqualTo(UserStatus.ACTIVE);
    }

    @Test
    @DisplayName("In production profile, skips creating accounts with default passwords")
    void testSkipsCreationInProductionProfile() {
        environment.setActiveProfiles("prod");
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        initializer.run(null);

        verify(userRepository, never()).save(any(User.class));
    }
}
