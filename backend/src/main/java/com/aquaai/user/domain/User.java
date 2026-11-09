package com.aquaai.user.domain;

import com.aquaai.shared.domain.BaseAuditableEntity;
import com.aquaai.shared.security.UserRole;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "users")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User extends BaseAuditableEntity {

    @Column(name = "first_name", nullable = false, length = 64)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 64)
    private String lastName;

    @Column(name = "full_name", length = 128)
    private String fullName;

    @Column(name = "phone_number", nullable = false, unique = true, length = 32)
    private String phoneNumber;

    @Column(name = "email", unique = true, length = 128)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 32)
    @Builder.Default
    private UserRole role = UserRole.FARMER;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    @Builder.Default
    private UserStatus status = UserStatus.ACTIVE;

    @Column(name = "district", length = 64)
    @Builder.Default
    private String district = "Yangiariq";

    @Column(name = "organization", length = 128)
    private String organization;

    @Column(name = "telegram_username", length = 64)
    private String telegramUsername;

    @Column(name = "preferred_language", length = 8)
    @Builder.Default
    private String preferredLanguage = "uz";

    public String getFullName() {
        if (fullName != null && !fullName.isBlank()) {
            return fullName;
        }
        String first = firstName != null ? firstName.trim() : "";
        String last = lastName != null ? lastName.trim() : "";
        String combined = (first + " " + last).trim();
        return combined.isEmpty() ? "User" : combined;
    }

    public boolean isAllowedToAuthenticate() {
        return this.status == UserStatus.ACTIVE;
    }

    public boolean isFarmer() {
        return this.role == UserRole.FARMER;
    }

    public boolean isOperator() {
        return this.role == UserRole.OPERATOR;
    }

    public boolean isAdmin() {
        return this.role == UserRole.ADMIN || this.role == UserRole.SUPER_ADMIN;
    }

    public boolean isSuperAdmin() {
        return this.role == UserRole.SUPER_ADMIN;
    }

    public boolean isActive() {
        return this.status == UserStatus.ACTIVE;
    }

    public boolean isBlocked() {
        return this.status == UserStatus.BLOCKED;
    }

    public void updateProfile(String firstName, String lastName, String email, String district) {
        updateProfile(firstName, lastName, email, district, this.organization, this.telegramUsername, this.preferredLanguage);
    }

    public void updateProfile(String firstName, String lastName, String email, String district,
                              String organization, String telegramUsername, String preferredLanguage) {
        if (firstName != null && !firstName.isBlank()) {
            this.firstName = firstName.trim();
        }
        if (lastName != null && !lastName.isBlank()) {
            this.lastName = lastName.trim();
        }
        this.fullName = (this.firstName + " " + this.lastName).trim();
        if (email != null) {
            this.email = email.trim().toLowerCase();
        }
        if (district != null && !district.isBlank()) {
            this.district = district.trim();
        }
        if (organization != null) {
            this.organization = organization.trim();
        }
        if (telegramUsername != null) {
            this.telegramUsername = telegramUsername.trim();
        }
        if (preferredLanguage != null && !preferredLanguage.isBlank()) {
            this.preferredLanguage = preferredLanguage.trim().toLowerCase();
        }
    }

    public void deactivate() {
        this.status = UserStatus.INACTIVE;
    }

    public void block() {
        this.status = UserStatus.BLOCKED;
    }

    public void activate() {
        this.status = UserStatus.ACTIVE;
    }

    public void changeStatus(UserStatus newStatus) {
        if (newStatus != null) {
            this.status = newStatus;
        }
    }

    public void changeRole(UserRole newRole) {
        if (newRole != null) {
            this.role = newRole;
        }
    }

    public void changePassword(String newPasswordHash) {
        this.passwordHash = newPasswordHash;
    }
}
