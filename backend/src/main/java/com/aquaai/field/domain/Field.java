package com.aquaai.field.domain;

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

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "fields")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Field extends BaseAuditableEntity {

    @Column(name = "farmer_id", nullable = false)
    private UUID farmerId;

    @Column(name = "name", nullable = false, length = 128)
    private String name;

    @Column(name = "area_hectares", nullable = false, precision = 10, scale = 2)
    private BigDecimal areaHectares;

    @Column(name = "crop_type", nullable = false, length = 64)
    private String cropType;

    @Column(name = "soil_type", length = 64)
    private String soilType;

    @Enumerated(EnumType.STRING)
    @Column(name = "irrigation_method", nullable = false, length = 32)
    private IrrigationMethod irrigationMethod;

    @Column(name = "coordinates_geojson", columnDefinition = "TEXT")
    private String coordinatesGeoJson;

    @Column(name = "canal_source", length = 128)
    private String canalSource;

    @Column(name = "active", nullable = false)
    @Builder.Default
    private boolean active = true;
}
