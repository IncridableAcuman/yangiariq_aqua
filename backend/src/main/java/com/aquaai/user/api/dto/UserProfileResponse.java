package com.aquaai.user.api.dto;

import com.aquaai.shared.security.UserRole;
import com.aquaai.user.domain.User;
import com.aquaai.user.domain.UserStatus;
import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

@Builder
public record UserProfileResponse(
        UUID id,
        String firstName,
        String lastName,
        String fullName,
        String phoneNumber,
        String email,
        UserRole role,
        UserStatus status,
        String district,
        Instant createdAt
) {
    public static UserProfileResponse from(User user) {
        return UserProfileResponse.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .fullName(user.getFullName())
                .phoneNumber(user.getPhoneNumber())
                .email(user.getEmail())
                .role(user.getRole())
                .status(user.getStatus())
                .district(user.getDistrict())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
