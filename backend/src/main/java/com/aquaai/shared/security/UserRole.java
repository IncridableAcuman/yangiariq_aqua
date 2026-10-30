package com.aquaai.shared.security;

public enum UserRole {
    FARMER,
    OPERATOR,
    ADMIN;

    public String getAuthority() {
        return "ROLE_" + name();
    }
}
