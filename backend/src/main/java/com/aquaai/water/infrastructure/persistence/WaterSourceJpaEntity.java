package com.aquaai.water.infrastructure.persistence;

import com.aquaai.shared.domain.BaseAuditableEntity;
import com.aquaai.water.domain.model.WaterSourceStatus;
import com.aquaai.water.domain.model.WaterSourceType;
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

/**
 * JPA entity representing the "water_sources" table.
 * Infrastructure persistence representation; never exposed to domain or API layers.
 */
@Getter
@Setter
@Entity
@Table(name = "water_sources")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WaterSourceJpaEntity extends BaseAuditableEntity {

    @Column(name = "name", nullable = false, length = 128)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 32)
    private WaterSourceType type;

    @Column(name = "description", length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    @Builder.Default
    private WaterSourceStatus status = WaterSourceStatus.ACTIVE;
}
