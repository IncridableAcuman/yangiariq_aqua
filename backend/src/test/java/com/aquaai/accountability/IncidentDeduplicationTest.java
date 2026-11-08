package com.aquaai.accountability;

import com.aquaai.accountability.domain.service.IncidentDeduplicationEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class IncidentDeduplicationTest {

    private IncidentDeduplicationEngine deduplicationEngine;

    @BeforeEach
    void setUp() {
        deduplicationEngine = new IncidentDeduplicationEngine();
    }

    @Test
    @DisplayName("Deduplication key is deterministic and case-insensitive")
    void shouldBuildDeterministicKey() {
        UUID entityId = UUID.randomUUID();
        String key1 = deduplicationEngine.buildDeduplicationKey("WATER_ACCOUNTING", "RECONCILIATION", entityId);
        String key2 = deduplicationEngine.buildDeduplicationKey("water_accounting", "reconciliation", entityId);

        assertEquals(key1, key2);
        assertEquals("water_accounting:reconciliation:" + entityId, key1);
    }

    @Test
    @DisplayName("Different source entity IDs yield distinct deduplication keys")
    void shouldDistinguishDifferentEntities() {
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        String key1 = deduplicationEngine.buildDeduplicationKey("WATER_ACCOUNTING", "RECONCILIATION", id1);
        String key2 = deduplicationEngine.buildDeduplicationKey("WATER_ACCOUNTING", "RECONCILIATION", id2);

        assertNotEquals(key1, key2);
    }
}
