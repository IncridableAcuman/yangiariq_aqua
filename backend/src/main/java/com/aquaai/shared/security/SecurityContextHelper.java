package com.aquaai.shared.security;

import com.aquaai.shared.domain.UnauthorizedOperationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

/**
 * Reusable utility for resource ownership verification and role-based checks.
 * Serves as the ownership-aware authorization foundation across all modules.
 */
public final class SecurityContextHelper {

    private SecurityContextHelper() {
    }

    public static Optional<UserPrincipal> getCurrentPrincipal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal principal) {
            return Optional.of(principal);
        }
        return Optional.empty();
    }

    public static UserPrincipal getRequiredPrincipal() {
        return getCurrentPrincipal()
                .orElseThrow(() -> new UnauthorizedOperationException("Authentication required to perform this operation"));
    }

    public static UUID getCurrentUserId() {
        return getRequiredPrincipal().getId();
    }

    public static UserRole getCurrentUserRole() {
        return getRequiredPrincipal().getRole();
    }

    public static boolean isCurrentUser(UUID targetUserId) {
        if (targetUserId == null) {
            return false;
        }
        return targetUserId.equals(getCurrentUserId());
    }

    public static boolean isSuperAdmin() {
        return getCurrentUserRole() == UserRole.SUPER_ADMIN;
    }

    public static boolean hasExactRole(UserRole role) {
        return getCurrentUserRole() == role;
    }

    public static boolean hasRole(UserRole role) {
        UserRole current = getCurrentUserRole();
        if (role == UserRole.ADMIN && current == UserRole.SUPER_ADMIN) {
            return true;
        }
        return current == role;
    }

    public static boolean hasAnyRole(UserRole... roles) {
        UserRole current = getCurrentUserRole();
        if (current == UserRole.SUPER_ADMIN) {
            return true;
        }
        return Arrays.stream(roles).anyMatch(r -> r == current);
    }

    /**
     * Verifies that the authenticated user is either the resource owner or holds an administrative override role.
     *
     * @param resourceOwnerId      the UUID of the resource owner
     * @param allowedOverrideRoles roles permitted to access the resource even if not the direct owner (e.g. ADMIN, OPERATOR)
     * @throws UnauthorizedOperationException if authorization fails
     */
    public static void verifyOwnershipOrRole(UUID resourceOwnerId, UserRole... allowedOverrideRoles) {
        if (isCurrentUser(resourceOwnerId)) {
            return;
        }
        if (hasAnyRole(allowedOverrideRoles)) {
            return;
        }
        throw new UnauthorizedOperationException("Access denied: You do not have permission to access or modify this resource");
    }
}
