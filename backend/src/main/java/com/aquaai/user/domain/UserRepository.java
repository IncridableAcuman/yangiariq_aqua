package com.aquaai.user.domain;

import com.aquaai.shared.security.UserRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByPhoneNumber(String phoneNumber);

    Optional<User> findByEmail(String email);

    @Query("SELECT u FROM User u WHERE u.phoneNumber = :identifier OR (u.email IS NOT NULL AND LOWER(u.email) = LOWER(:identifier))")
    Optional<User> findByIdentifier(@Param("identifier") String identifier);

    boolean existsByPhoneNumber(String phoneNumber);

    boolean existsByPhoneNumberAndIdNot(String phoneNumber, UUID id);

    boolean existsByEmail(String email);

    boolean existsByEmailAndIdNot(String email, UUID id);

    long countByRole(UserRole role);

    long countByStatus(UserStatus status);

    @Query("""
        SELECT u FROM User u
        WHERE (:search IS NULL OR :search = '' OR
               LOWER(u.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR
               LOWER(u.lastName) LIKE LOWER(CONCAT('%', :search, '%')) OR
               LOWER(u.fullName) LIKE LOWER(CONCAT('%', :search, '%')) OR
               LOWER(u.phoneNumber) LIKE LOWER(CONCAT('%', :search, '%')) OR
               (u.email IS NOT NULL AND LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%'))))
          AND (:role IS NULL OR u.role = :role)
          AND (:status IS NULL OR u.status = :status)
          AND (:district IS NULL OR :district = '' OR LOWER(u.district) = LOWER(:district))
    """)
    Page<User> findUsersWithFilters(
            @Param("search") String search,
            @Param("role") UserRole role,
            @Param("status") UserStatus status,
            @Param("district") String district,
            Pageable pageable
    );
}
