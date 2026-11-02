package com.aquaai.operations;

import com.aquaai.irrigation.domain.model.IrrigationPlanStatus;
import com.aquaai.irrigation.domain.model.SmartIrrigationPlan;
import com.aquaai.irrigation.domain.model.WaterBudget;
import com.aquaai.irrigation.domain.port.SmartIrrigationPlanRepository;
import com.aquaai.operations.api.dto.ScheduleConflictDto;
import com.aquaai.operations.application.service.ScheduleConflictDetectionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ScheduleConflictDetectionServiceUnitTest {

    @Mock
    private SmartIrrigationPlanRepository planRepository;

    private ScheduleConflictDetectionService conflictService;

    private UUID fieldId;
    private Instant now;

    @BeforeEach
    void setUp() {
        conflictService = new ScheduleConflictDetectionService(planRepository);
        fieldId = UUID.randomUUID();
        now = Instant.now();
    }

    @Test
    @DisplayName("Detects HYDRAULIC_BLOCK when suspected pipe burst is active")
    void detectsHydraulicBlockOnPipeBurst() {
        ScheduleConflictDto conflict = conflictService.detectConflict(
                fieldId,
                null,
                now,
                now.plusSeconds(3600),
                BigDecimal.valueOf(400),
                null,
                true,
                false
        );

        assertThat(conflict.isHasConflict()).isTrue();
        assertThat(conflict.getConflictType()).isEqualTo("HYDRAULIC_BLOCK");
        assertThat(conflict.getConflictMessage()).contains("Hydraulic safety interlock active");
    }

    @Test
    @DisplayName("Detects INSUFFICIENT_WATER when requested volume exceeds budget")
    void detectsInsufficientWaterConflict() {
        WaterBudget budget = WaterBudget.builder()
                .fieldId(fieldId)
                .totalAllocatedM3(BigDecimal.valueOf(1000))
                .consumedM3(BigDecimal.valueOf(800))
                .committedM3(BigDecimal.ZERO)
                .reservedM3(BigDecimal.ZERO)
                .remainingAvailableM3(BigDecimal.valueOf(200))
                .build();

        ScheduleConflictDto conflict = conflictService.detectConflict(
                fieldId,
                null,
                now,
                now.plusSeconds(3600),
                BigDecimal.valueOf(350), // 350 > 200
                budget,
                false,
                false
        );

        assertThat(conflict.isHasConflict()).isTrue();
        assertThat(conflict.getConflictType()).isEqualTo("INSUFFICIENT_WATER");
        assertThat(conflict.getConflictMessage()).contains("exceeds remaining seasonal allocation");
    }

    @Test
    @DisplayName("Detects TIME_OVERLAP when another plan is scheduled in the same window")
    void detectsTimeOverlapConflict() {
        Instant existingStart = now.plusSeconds(1800); // starts in 30 min
        Instant existingEnd = now.plusSeconds(5400);   // ends in 90 min

        SmartIrrigationPlan existingPlan = SmartIrrigationPlan.createDraft(
                fieldId,
                UUID.randomUUID(),
                null,
                LocalDate.now(),
                existingStart,
                existingEnd,
                BigDecimal.valueOf(300),
                60,
                BigDecimal.valueOf(50),
                BigDecimal.valueOf(60),
                BigDecimal.valueOf(20),
                "Existing scheduled plan",
                null,
                Collections.emptyList(),
                Collections.emptyList()
        );
        existingPlan.approve(UUID.randomUUID());
        existingPlan.schedule();

        when(planRepository.findByFieldIdIn(any())).thenReturn(List.of(existingPlan));

        // Requesting overlapping window: now to now + 3600 (overlaps with existingStart at 1800)
        ScheduleConflictDto conflict = conflictService.detectConflict(
                fieldId,
                UUID.randomUUID(), // different plan ID
                now,
                now.plusSeconds(3600),
                BigDecimal.valueOf(200),
                null,
                false,
                false
        );

        assertThat(conflict.isHasConflict()).isTrue();
        assertThat(conflict.getConflictType()).isEqualTo("TIME_OVERLAP");
        assertThat(conflict.getConflictMessage()).contains("Schedule conflict: Field already has an irrigation turn scheduled");
    }

    @Test
    @DisplayName("Returns no conflict when windows do not overlap and budget is sufficient")
    void returnsNoConflictWhenConditionsAreValid() {
        WaterBudget budget = WaterBudget.builder()
                .fieldId(fieldId)
                .totalAllocatedM3(BigDecimal.valueOf(1000))
                .consumedM3(BigDecimal.valueOf(200))
                .committedM3(BigDecimal.ZERO)
                .reservedM3(BigDecimal.ZERO)
                .remainingAvailableM3(BigDecimal.valueOf(800))
                .build();

        when(planRepository.findByFieldIdIn(any())).thenReturn(Collections.emptyList());

        ScheduleConflictDto conflict = conflictService.detectConflict(
                fieldId,
                UUID.randomUUID(),
                now,
                now.plusSeconds(3600),
                BigDecimal.valueOf(300),
                budget,
                false,
                false
        );

        assertThat(conflict.isHasConflict()).isFalse();
        assertThat(conflict.getConflictType()).isNull();
    }
}
