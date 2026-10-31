package com.aquaai.shared.security;

import java.util.Optional;
import java.util.UUID;

/**
 * Port abstraction allowing application use cases to retrieve current authenticated user
 * identity without coupling directly to Spring Security's SecurityContext.
 */
public interface CurrentUserProvider {

    Optional<UUID> getCurrentUserId();

    UUID getRequiredUserId();

    Optional<UserRole> getCurrentUserRole();

    UserRole getRequiredUserRole();

    boolean isAuthenticated();
}
