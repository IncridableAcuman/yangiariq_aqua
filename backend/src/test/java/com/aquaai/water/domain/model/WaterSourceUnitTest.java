package com.aquaai.water.domain.model;

import com.aquaai.shared.domain.BusinessRuleException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WaterSourceUnitTest {

    @Nested
    @DisplayName("WaterSource Invariants & Creation")
    class CreationTests {

        @Test
        @DisplayName("Valid water source creation sets properties and defaults to ACTIVE")
        void shouldCreateValidWaterSource() {
            WaterSource source = WaterSource.create(
                    "Palvon Canal Main Line",
                    WaterSourceType.CANAL,
                    "Primary distribution canal serving southern Yangiariq farms"
            );

            assertThat(source.getId()).isNotNull();
            assertThat(source.getName()).isEqualTo("Palvon Canal Main Line");
            assertThat(source.getType()).isEqualTo(WaterSourceType.CANAL);
            assertThat(source.getDescription()).isEqualTo("Primary distribution canal serving southern Yangiariq farms");
            assertThat(source.getStatus()).isEqualTo(WaterSourceStatus.ACTIVE);
            assertThat(source.isActive()).isTrue();
            assertThat(source.isInactive()).isFalse();
            assertThat(source.getCreatedAt()).isNotNull();
            assertThat(source.getUpdatedAt()).isNotNull();
            assertThat(source.getVersion()).isNull();
        }

        @Test
        @DisplayName("Creation with explicit ID preserves provided UUID")
        void shouldCreateWithExplicitId() {
            UUID customId = UUID.randomUUID();
            WaterSource source = WaterSource.create(
                    customId,
                    "Qiyot Ditch System",
                    WaterSourceType.DITCH,
                    null
            );

            assertThat(source.getId()).isEqualTo(customId);
            assertThat(source.getName()).isEqualTo("Qiyot Ditch System");
            assertThat(source.getType()).isEqualTo(WaterSourceType.DITCH);
            assertThat(source.getDescription()).isNull();
        }

        @ParameterizedTest
        @ValueSource(strings = {"", "   ", "\t\n"})
        @DisplayName("Blank water source name throws BusinessRuleException")
        void shouldRejectBlankName(String blankName) {
            assertThatThrownBy(() -> WaterSource.create(blankName, WaterSourceType.CANAL, "Valid desc"))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("Water source name cannot be blank");
        }

        @Test
        @DisplayName("Null water source name throws BusinessRuleException")
        void shouldRejectNullName() {
            assertThatThrownBy(() -> WaterSource.create(null, WaterSourceType.CANAL, "Valid desc"))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("Water source name cannot be blank");
        }

        @Test
        @DisplayName("Name exceeding 128 characters throws BusinessRuleException")
        void shouldRejectOversizedName() {
            String oversized = "A".repeat(129);
            assertThatThrownBy(() -> WaterSource.create(oversized, WaterSourceType.CANAL, null))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("cannot exceed 128 characters");
        }

        @Test
        @DisplayName("Null water source type throws BusinessRuleException")
        void shouldRejectNullType() {
            assertThatThrownBy(() -> WaterSource.create("Valid Name", null, "Valid desc"))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("Water source type must not be null");
        }

        @Test
        @DisplayName("Description exceeding 500 characters throws BusinessRuleException")
        void shouldRejectOversizedDescription() {
            String oversized = "B".repeat(501);
            assertThatThrownBy(() -> WaterSource.create("Valid Canal", WaterSourceType.CANAL, oversized))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("cannot exceed 500 characters");
        }
    }

    @Nested
    @DisplayName("Lifecycle & Update Tests")
    class LifecycleTests {

        @Test
        @DisplayName("Update details validates invariants and updates timestamps")
        void shouldUpdateDetails() {
            WaterSource source = WaterSource.create("Original Canal", WaterSourceType.CANAL, "Old info");
            Instant beforeUpdate = source.getUpdatedAt();

            source.updateDetails("Updated Canal Line 4", WaterSourceType.RESERVOIR, "Upgraded pump capacity");

            assertThat(source.getName()).isEqualTo("Updated Canal Line 4");
            assertThat(source.getType()).isEqualTo(WaterSourceType.RESERVOIR);
            assertThat(source.getDescription()).isEqualTo("Upgraded pump capacity");
            assertThat(source.getUpdatedAt()).isAfterOrEqualTo(beforeUpdate);
        }

        @Test
        @DisplayName("Deactivate transitions status to INACTIVE")
        void shouldDeactivate() {
            WaterSource source = WaterSource.create("Deep Well #3", WaterSourceType.WELL, null);
            assertThat(source.isActive()).isTrue();

            source.deactivate();

            assertThat(source.getStatus()).isEqualTo(WaterSourceStatus.INACTIVE);
            assertThat(source.isActive()).isFalse();
            assertThat(source.isInactive()).isTrue();
        }

        @Test
        @DisplayName("Activate transitions status back to ACTIVE")
        void shouldActivate() {
            WaterSource source = WaterSource.create("Deep Well #3", WaterSourceType.WELL, null);
            source.deactivate();
            assertThat(source.isInactive()).isTrue();

            source.activate();

            assertThat(source.getStatus()).isEqualTo(WaterSourceStatus.ACTIVE);
            assertThat(source.isActive()).isTrue();
        }
    }

    @Nested
    @DisplayName("WaterSource Geographic Coordinates Tests")
    class CoordinatesTests {

        @Test
        @DisplayName("Valid coordinates are preserved and rounded to 6 decimal places")
        void shouldAcceptValidCoordinates() {
            WaterSource source = WaterSource.create(
                    "Palvon Main Canal Intake",
                    WaterSourceType.CANAL,
                    "Canal intake point",
                    new java.math.BigDecimal("41.3456789"),
                    new java.math.BigDecimal("60.6123454")
            );

            assertThat(source.getLatitude()).isNotNull();
            assertThat(source.getLatitude()).isEqualByComparingTo("41.345679");
            assertThat(source.getLongitude()).isNotNull();
            assertThat(source.getLongitude()).isEqualByComparingTo("60.612345");
        }

        @Test
        @DisplayName("Providing only latitude throws BusinessRuleException")
        void shouldRejectPartialLatitude() {
            assertThatThrownBy(() -> WaterSource.create(
                    "Canal",
                    WaterSourceType.CANAL,
                    null,
                    new java.math.BigDecimal("41.34"),
                    null
            ))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("Both latitude and longitude must be provided together");
        }

        @Test
        @DisplayName("Providing only longitude throws BusinessRuleException")
        void shouldRejectPartialLongitude() {
            assertThatThrownBy(() -> WaterSource.create(
                    "Canal",
                    WaterSourceType.CANAL,
                    null,
                    null,
                    new java.math.BigDecimal("60.61")
            ))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("Both latitude and longitude must be provided together");
        }

        @Test
        @DisplayName("Latitude out of range [-90.0, 90.0] throws BusinessRuleException")
        void shouldRejectOutOfRangeLatitude() {
            assertThatThrownBy(() -> WaterSource.create(
                    "Canal",
                    WaterSourceType.CANAL,
                    null,
                    new java.math.BigDecimal("91.0"),
                    new java.math.BigDecimal("60.0")
            ))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("between -90.0 and 90.0 degrees");
        }

        @Test
        @DisplayName("Longitude out of range [-180.0, 180.0] throws BusinessRuleException")
        void shouldRejectOutOfRangeLongitude() {
            assertThatThrownBy(() -> WaterSource.create(
                    "Canal",
                    WaterSourceType.CANAL,
                    null,
                    new java.math.BigDecimal("41.0"),
                    new java.math.BigDecimal("185.0")
            ))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("between -180.0 and 180.0 degrees");
        }

        @Test
        @DisplayName("Update details updates coordinates correctly")
        void shouldUpdateCoordinates() {
            WaterSource source = WaterSource.create("Canal", WaterSourceType.CANAL, null);
            assertThat(source.getLatitude()).isNull();
            assertThat(source.getLongitude()).isNull();

            source.updateDetails(
                    "Canal Updated",
                    WaterSourceType.CANAL,
                    "Relocated intake",
                    new java.math.BigDecimal("41.500000"),
                    new java.math.BigDecimal("60.700000")
            );

            assertThat(source.getLatitude()).isEqualByComparingTo("41.500000");
            assertThat(source.getLongitude()).isEqualByComparingTo("60.700000");
        }
    }

    @Nested
    @DisplayName("Enum Multilingual Support Tests")
    class EnumTests {

        @Test
        @DisplayName("WaterSourceType provides localized display names")
        void shouldProvideLocalizedTypeNames() {
            assertThat(WaterSourceType.CANAL.getDisplayName("uz")).isEqualTo("Kanal");
            assertThat(WaterSourceType.CANAL.getDisplayName("ru")).isEqualTo("Канал");
            assertThat(WaterSourceType.CANAL.getDisplayName("en")).isEqualTo("Canal");
            assertThat(WaterSourceType.CANAL.getDisplayName(null)).isEqualTo("Kanal");
        }

        @Test
        @DisplayName("WaterSourceStatus provides localized display names")
        void shouldProvideLocalizedStatusNames() {
            assertThat(WaterSourceStatus.ACTIVE.getDisplayName("uz")).isEqualTo("Faol");
            assertThat(WaterSourceStatus.ACTIVE.getDisplayName("ru")).isEqualTo("Активный");
            assertThat(WaterSourceStatus.ACTIVE.getDisplayName("en")).isEqualTo("Active");
        }
    }
}
