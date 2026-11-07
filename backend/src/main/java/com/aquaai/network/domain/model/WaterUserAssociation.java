package com.aquaai.network.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Entity representing a local Water Users Association (WUA / Suv Iste'molchilari Uyushmasi)
 * responsible for sub-district secondary canal water allocation and local delivery coordination.
 */
@Getter
@Builder(toBuilder = true)
public class WaterUserAssociation {

    private final UUID id;
    private final String name;
    private final String code;
    private final String district;
    private final String contactPerson;
    private final String phoneNumber;
    private final Instant createdAt;
    private Instant updatedAt;

    public WaterUserAssociation(
            UUID id,
            String name,
            String code,
            String district,
            String contactPerson,
            String phoneNumber,
            Instant createdAt,
            Instant updatedAt
    ) {
        this.id = id != null ? id : UUID.randomUUID();
        this.name = Objects.requireNonNull(name, "WUA name must not be null");
        this.code = Objects.requireNonNull(code, "WUA code must not be null");
        this.district = district != null ? district : "Yangiariq";
        this.contactPerson = contactPerson;
        this.phoneNumber = phoneNumber;
        this.createdAt = createdAt != null ? createdAt : Instant.now();
        this.updatedAt = updatedAt != null ? updatedAt : this.createdAt;
    }
}
