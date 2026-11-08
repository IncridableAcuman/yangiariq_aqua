package com.aquaai.accountability.infrastructure.persistence.entity;

import com.aquaai.accountability.domain.model.VerificationOutcome;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "incident_verifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IncidentVerificationJpaEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "incident_id", nullable = false)
    private UUID incidentId;

    @Column(name = "action_id")
    private UUID actionId;

    @Column(name = "verifier_id", nullable = false)
    private UUID verifierId;

    @Column(name = "verifier_name", nullable = false, length = 255)
    private String verifierName;

    @Column(name = "verified_at", nullable = false)
    private Instant verifiedAt;

    @Column(name = "verification_method", nullable = false, length = 128)
    private String verificationMethod;

    @Enumerated(EnumType.STRING)
    @Column(name = "result", nullable = false, length = 64)
    private VerificationOutcome result;

    @Column(name = "explanatory_notes", nullable = false, length = 2000)
    private String explanatoryNotes;

    @Column(name = "supporting_evidence_uri", length = 1000)
    private String supportingEvidenceUri;
}
