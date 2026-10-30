package com.aquaai.auth.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RefreshTokenSessionRepository extends JpaRepository<RefreshTokenSession, UUID> {

    Optional<RefreshTokenSession> findByTokenHash(String tokenHash);

    List<RefreshTokenSession> findAllByUserIdAndRevokedAtIsNull(UUID userId);

    @Modifying
    @Query("UPDATE RefreshTokenSession s SET s.revokedAt = CURRENT_TIMESTAMP WHERE s.userId = :userId AND s.revokedAt IS NULL")
    void revokeAllActiveSessionsForUser(@Param("userId") UUID userId);
}
