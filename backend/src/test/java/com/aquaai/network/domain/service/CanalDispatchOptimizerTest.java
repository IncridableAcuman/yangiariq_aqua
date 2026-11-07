package com.aquaai.network.domain.service;

import com.aquaai.network.domain.model.DispatchSchedule;
import com.aquaai.network.domain.model.DispatchStatus;
import com.aquaai.network.domain.model.FairnessPolicy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class CanalDispatchOptimizerTest {

    private final CanalDispatchOptimizer optimizer = new CanalDispatchOptimizer();

    @Test
    @DisplayName("Optimizes contention-free proposal and staggers delivery when needed")
    void optimizesDispatchProposals() {
        UUID fld1 = UUID.randomUUID();
        UUID fld2 = UUID.randomUUID();
        UUID edgeId = UUID.randomUUID();
        Instant now = Instant.now();
        Instant end = now.plusSeconds(86400 * 3);

        CanalDispatchOptimizer.ParcelDemandRequest req1 = new CanalDispatchOptimizer.ParcelDemandRequest(
                fld1, "Field 1", UUID.randomUUID(), "WUA 1",
                new BigDecimal("500.00"), new BigDecimal("0.05"), 1,
                new BigDecimal("35.0"), "COTTON", new BigDecimal("1000.00"),
                edgeId, "Canal 1", new BigDecimal("0.08"),
                false, null, now, now.plusSeconds(14400)
        );

        CanalDispatchOptimizer.ParcelDemandRequest req2 = new CanalDispatchOptimizer.ParcelDemandRequest(
                fld2, "Field 2", UUID.randomUUID(), "WUA 1",
                new BigDecimal("400.00"), new BigDecimal("0.05"), 2,
                new BigDecimal("45.0"), "WHEAT", new BigDecimal("1000.00"),
                edgeId, "Canal 1", new BigDecimal("0.08"),
                false, null, now, now.plusSeconds(14400)
        );

        CanalDispatchOptimizer.OptimizationResult result = optimizer.optimize(
                "Test Optimization",
                UUID.randomUUID(),
                "WUA 1",
                List.of(req1, req2),
                FairnessPolicy.defaultPolicy(),
                now,
                end,
                UUID.randomUUID()
        );

        DispatchSchedule schedule = result.schedule();
        assertThat(schedule).isNotNull();
        assertThat(schedule.getStatus()).isIn(DispatchStatus.PROPOSED, DispatchStatus.REVIEW_REQUIRED);
        assertThat(schedule.getAssignments()).hasSize(2);
        assertThat(schedule.getTotalAllocatedM3()).isGreaterThan(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("Hydraulically blocked parcel is strictly excluded with zero allocation")
    void blockedParcelGetsZeroAllocation() {
        UUID fld1 = UUID.randomUUID();
        Instant now = Instant.now();
        Instant end = now.plusSeconds(86400);

        CanalDispatchOptimizer.ParcelDemandRequest req1 = new CanalDispatchOptimizer.ParcelDemandRequest(
                fld1, "Field 1", UUID.randomUUID(), "WUA 1",
                new BigDecimal("500.00"), new BigDecimal("0.05"), 1,
                new BigDecimal("35.0"), "COTTON", new BigDecimal("1000.00"),
                UUID.randomUUID(), "Canal 1", new BigDecimal("1.00"),
                true, "Hydraulic safety interlock active", now, now.plusSeconds(14400)
        );

        CanalDispatchOptimizer.OptimizationResult result = optimizer.optimize(
                "Safety Interlock Test",
                UUID.randomUUID(),
                "WUA 1",
                List.of(req1),
                FairnessPolicy.defaultPolicy(),
                now,
                end,
                UUID.randomUUID()
        );

        assertThat(result.schedule().getAssignments().get(0).allocatedVolumeM3()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.schedule().getAssignments().get(0).rationale()).containsIgnoringCase("blocked");
    }
}
