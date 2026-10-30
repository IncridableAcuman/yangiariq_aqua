package com.aquaai.water.domain;

import com.aquaai.shared.domain.BaseAuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "irrigation_records")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IrrigationRecord extends BaseAuditableEntity {

    @Column(name = "field_id", nullable = false)
    private UUID fieldId;

    @Column(name = "start_time", nullable = false)
    private Instant startTime;

    @Column(name = "end_time")
    private Instant endTime;

    @Column(name = "water_volume_m3", nullable = false, precision = 12, scale = 2)
    private BigDecimal waterVolumeM3;

    @Column(name = "water_source", length = 128)
    private String waterSource;

    @Column(name = "recorded_by", nullable = false)
    private UUID recordedBy;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
}
