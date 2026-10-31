package com.aquaai.shared.security;

import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

/**
 * Spring Security adapter implementing CurrentUserProvider.
 */
@Component
public class SpringSecurityCurrentUserProvider implements CurrentUserProvider {

    @Override
    public Optional<UUID> getCurrentUserId() {
        return SecurityContextHelper.getCurrentPrincipal().map(UserPrincipal::getId);
    }

    @Override
    public UUID getRequiredUserId() {
        return SecurityContextHelper.getCurrentUserId();
    }

    @Override
    public Optional<UserRole> getCurrentUserRole() {
        return SecurityContextHelper.getCurrentPrincipal().map(UserPrincipal::getRole);
    }

    @Override
    public UserRole getRequiredUserRole() {
        return SecurityContextHelper.getCurrentUserRole();
    }

    @Override
    public boolean isAuthenticated() {
        return SecurityContextHelper.getCurrentPrincipal().isPresent();
    }
}
