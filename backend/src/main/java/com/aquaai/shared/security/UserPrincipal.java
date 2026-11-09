package com.aquaai.shared.security;

import lombok.Builder;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Getter
@Builder
public class UserPrincipal implements UserDetails {

    private final UUID id;
    private final String username;
    private final String password;
    private final UserRole role;
    private final boolean enabled;
    @Builder.Default
    private final Set<String> permissions = new HashSet<>();

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Set<GrantedAuthority> authorities = new HashSet<>();
        authorities.add(new SimpleGrantedAuthority(role.getAuthority()));
        if (role == UserRole.SUPER_ADMIN) {
            authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
            authorities.add(new SimpleGrantedAuthority("ROLE_OPERATOR"));
        }
        Set<String> perms = (permissions != null && !permissions.isEmpty())
                ? permissions
                : role.getDefaultPermissions();
        for (String perm : perms) {
            authorities.add(new SimpleGrantedAuthority(perm));
        }
        return authorities;
    }

    public boolean hasPermission(String permission) {
        if (role == UserRole.SUPER_ADMIN) {
            return true;
        }
        Set<String> perms = (permissions != null && !permissions.isEmpty())
                ? permissions
                : role.getDefaultPermissions();
        return perms.contains(permission);
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
