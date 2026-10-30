package com.aquaai.field.application;

import com.aquaai.field.domain.model.Field;
import com.aquaai.field.domain.repository.FieldRepository;
import com.aquaai.shared.domain.ResourceNotFoundException;
import com.aquaai.shared.domain.UnauthorizedOperationException;
import com.aquaai.shared.security.UserRole;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.UUID;

/**
 * Reusable ownership verification abstraction for agricultural Field parcels.
 * Designed to be consumed by Field, Water, Problem, Sensor, and AI modules.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FieldOwnershipChecker {

    private final FieldRepository fieldRepository;

    /**
     * Verifies that the specified user is the direct owner of the field.
     *
     * @param fieldId Field identifier
     * @param userId  User identifier attempting access
     * @throws ResourceNotFoundException      if the field does not exist
     * @throws UnauthorizedOperationException if the user is not the field owner
     */
    public void verifyOwnership(UUID fieldId, UUID userId) {
        if (fieldId == null || userId == null) {
            throw new UnauthorizedOperationException("Field ID and User ID are required for ownership verification");
        }

        Field field = fieldRepository.findById(fieldId)
                .orElseThrow(() -> new ResourceNotFoundException("Field", fieldId));

        if (!field.isOwnedBy(userId)) {
            log.warn("Unauthorized access attempt: user {} does not own field {}", userId, fieldId);
            throw new UnauthorizedOperationException("Access denied: You do not have ownership of this field");
        }
    }

    /**
     * Verifies ownership or permits access if the user possesses an administrative/operator override role.
     *
     * @param fieldId              Field identifier
     * @param userId               User identifier
     * @param userRole             Role of the current user
     * @param allowedOverrideRoles Roles allowed to bypass direct ownership check (e.g. ADMIN, OPERATOR)
     */
    public void verifyOwnershipOrRole(UUID fieldId, UUID userId, UserRole userRole, UserRole... allowedOverrideRoles) {
        if (userRole != null && Arrays.asList(allowedOverrideRoles).contains(userRole)) {
            if (!fieldRepository.existsById(fieldId)) {
                throw new ResourceNotFoundException("Field", fieldId);
            }
            return;
        }
        verifyOwnership(fieldId, userId);
    }

    /**
     * Evaluates whether a user owns the field without throwing an exception.
     */
    public boolean isOwner(UUID fieldId, UUID userId) {
        if (fieldId == null || userId == null) {
            return false;
        }
        return fieldRepository.existsByOwnerIdAndId(userId, fieldId);
    }

    /**
     * Loads and returns the field only if the caller is the owner.
     */
    public Field getFieldIfOwner(UUID fieldId, UUID userId) {
        verifyOwnership(fieldId, userId);
        return fieldRepository.findById(fieldId)
                .orElseThrow(() -> new ResourceNotFoundException("Field", fieldId));
    }

    /**
     * Loads and returns the field if the caller is either the owner or holds an override role.
     */
    public Field getFieldIfAccessible(UUID fieldId, UUID userId, UserRole userRole, UserRole... allowedOverrideRoles) {
        verifyOwnershipOrRole(fieldId, userId, userRole, allowedOverrideRoles);
        return fieldRepository.findById(fieldId)
                .orElseThrow(() -> new ResourceNotFoundException("Field", fieldId));
    }
}
