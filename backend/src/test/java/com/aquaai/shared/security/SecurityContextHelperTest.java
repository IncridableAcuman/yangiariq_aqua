package com.aquaai.shared.security;

import com.aquaai.shared.domain.UnauthorizedOperationException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SecurityContextHelperTest {

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void authenticateAs(UUID userId, UserRole role) {
        UserPrincipal principal = UserPrincipal.builder()
                .id(userId)
                .username("+998901234567")
                .role(role)
                .enabled(true)
                .build();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities())
        );
    }

    @Test
    @DisplayName("Owner should be allowed to access their own resource")
    void testOwnershipAllowedForOwner() {
        UUID ownerId = UUID.randomUUID();
        authenticateAs(ownerId, UserRole.FARMER);

        assertThatCode(() -> SecurityContextHelper.verifyOwnershipOrRole(ownerId))
                .doesNotThrowAnyException();
        assertThat(SecurityContextHelper.isCurrentUser(ownerId)).isTrue();
    }

    @Test
    @DisplayName("Admin should be allowed to access another user's resource when override is specified")
    void testOwnershipAllowedForAdminOverride() {
        UUID ownerId = UUID.randomUUID();
        UUID adminId = UUID.randomUUID();
        authenticateAs(adminId, UserRole.ADMIN);

        assertThatCode(() -> SecurityContextHelper.verifyOwnershipOrRole(ownerId, UserRole.ADMIN))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Operator should be allowed when specified in allowed override roles")
    void testOwnershipAllowedForOperatorOverride() {
        UUID ownerId = UUID.randomUUID();
        UUID operatorId = UUID.randomUUID();
        authenticateAs(operatorId, UserRole.OPERATOR);

        assertThatCode(() -> SecurityContextHelper.verifyOwnershipOrRole(ownerId, UserRole.OPERATOR, UserRole.ADMIN))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Non-owner without override role must be rejected with UnauthorizedOperationException")
    void testOwnershipRejectedForDifferentFarmer() {
        UUID ownerId = UUID.randomUUID();
        UUID otherFarmerId = UUID.randomUUID();
        authenticateAs(otherFarmerId, UserRole.FARMER);

        assertThatThrownBy(() -> SecurityContextHelper.verifyOwnershipOrRole(ownerId, UserRole.ADMIN))
                .isInstanceOf(UnauthorizedOperationException.class)
                .hasMessageContaining("Access denied");
    }
}
