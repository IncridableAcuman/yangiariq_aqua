package com.aquaai.field.infrastructure.persistence;

import com.aquaai.field.domain.model.CropType;
import com.aquaai.field.domain.model.FieldStatus;
import com.aquaai.field.domain.model.IrrigationMethod;
import com.aquaai.field.domain.model.SoilType;
import com.aquaai.shared.domain.BaseAuditableEntity;
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
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * JPA entity representing the "fields" relational table.
 * Purely an infrastructure persistence model; never exposed to domain or API layers.
 */
@Getter
@Setter
@Entity
@Table(name = "fields")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FieldJpaEntity extends BaseAuditableEntity {

    @Column(name = "owner_id", nullable = false)
    private UUID ownerId;

    @Column(name = "name", nullable = false, length = 128)
    private String name;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "area_hectares", nullable = false, precision = 10, scale = 4)
    private BigDecimal areaHectares;

    @Enumerated(EnumType.STRING)
    @Column(name = "crop_type", nullable = false, length = 32)
    private CropType cropType;

    @Enumerated(EnumType.STRING)
    @Column(name = "soil_type", nullable = false, length = 32)
    private SoilType soilType;

    @Enumerated(EnumType.STRING)
    @Column(name = "irrigation_method", nullable = false, length = 32)
    private IrrigationMethod irrigationMethod;

    @Column(name = "latitude", precision = 9, scale = 6)
    private BigDecimal latitude;

    @Column(name = "longitude", precision = 9, scale = 6)
    private BigDecimal longitude;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "boundary", columnDefinition = "jsonb")
    private String boundary;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    @Builder.Default
    private FieldStatus status = FieldStatus.ACTIVE;
}
