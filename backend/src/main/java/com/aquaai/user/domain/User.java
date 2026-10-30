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

    public void updateProfile(String firstName, String lastName, String email, String district) {
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
    }

    public void changePassword(String newPasswordHash) {
        this.passwordHash = newPasswordHash;
    }
}
