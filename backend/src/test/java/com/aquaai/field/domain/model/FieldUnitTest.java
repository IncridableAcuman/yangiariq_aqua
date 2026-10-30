package com.aquaai.field.domain.model;

import com.aquaai.shared.domain.BusinessRuleException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FieldUnitTest {

    private final UUID ownerId = UUID.randomUUID();

    private Field createValidField() {
        return Field.create(
                ownerId,
                "Paxtakor Plot 1",
                "Main cotton field near Yangiariq canal",
                new BigDecimal("12.5000"),
                CropType.COTTON,
                SoilType.LOAM,
                IrrigationMethod.FURROW,
                FieldLocation.of(41.450123, 60.590456),
                FieldBoundary.fromExteriorRing(List.of(
                        CoordinatePoint.of(60.5900, 41.4500),
                        CoordinatePoint.of(60.5950, 41.4500),
                        CoordinatePoint.of(60.5950, 41.4550),
                        CoordinatePoint.of(60.5900, 41.4550)
                ))
        );
    }

    @Nested
    @DisplayName("Field Creation & Invariant Tests")
    class CreationTests {

        @Test
        @DisplayName("Valid field creation initializes all properties and defaults status to ACTIVE")
        void shouldCreateValidField() {
            Field field = createValidField();

            assertThat(field.getId()).isNotNull();
            assertThat(field.getOwnerId()).isEqualTo(ownerId);
            assertThat(field.getName()).isEqualTo("Paxtakor Plot 1");
            assertThat(field.getDescription()).isEqualTo("Main cotton field near Yangiariq canal");
            assertThat(field.getAreaHectares()).isEqualByComparingTo("12.5000");
            assertThat(field.getCropType()).isEqualTo(CropType.COTTON);
            assertThat(field.getSoilType()).isEqualTo(SoilType.LOAM);
            assertThat(field.getIrrigationMethod()).isEqualTo(IrrigationMethod.FURROW);
            assertThat(field.getLocation()).isNotNull();
            assertThat(field.getBoundary()).isNotNull();
            assertThat(field.getStatus()).isEqualTo(FieldStatus.ACTIVE);
            assertThat(field.isActive()).isTrue();
            assertThat(field.isInactive()).isFalse();
            assertThat(field.getCreatedAt()).isNotNull();
            assertThat(field.getUpdatedAt()).isNotNull();
        }

        @ParameterizedTest
        @ValueSource(strings = {"", "   ", "\t\n"})
        @DisplayName("Blank field name is rejected")
        void shouldRejectBlankName(String blankName) {
            assertThatThrownBy(() -> Field.create(
                    ownerId,
                    blankName,
                    "Description",
                    new BigDecimal("5.0"),
                    CropType.WHEAT,
                    SoilType.SANDY_LOAM,
                    IrrigationMethod.CANAL,
                    null,
                    null
            )).isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("Field name cannot be blank");
        }

        @Test
        @DisplayName("Null field name is rejected")
        void shouldRejectNullName() {
            assertThatThrownBy(() -> Field.create(
                    ownerId,
                    null,
                    "Description",
                    new BigDecimal("5.0"),
                    CropType.WHEAT,
                    SoilType.SANDY_LOAM,
                    IrrigationMethod.CANAL,
                    null,
                    null
            )).isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("Field name cannot be blank");
        }

        @Test
        @DisplayName("Field name exceeding 128 characters is rejected")
        void shouldRejectExcessiveNameLength() {
            String excessiveName = "A".repeat(129);
            assertThatThrownBy(() -> Field.create(
                    ownerId,
                    excessiveName,
                    "Description",
                    new BigDecimal("5.0"),
                    CropType.WHEAT,
                    SoilType.SANDY_LOAM,
                    IrrigationMethod.CANAL,
                    null,
                    null
            )).isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("cannot exceed 128 characters");
        }

        @Test
        @DisplayName("Null owner ID is rejected")
        void shouldRejectNullOwnerId() {
            assertThatThrownBy(() -> Field.create(
                    null,
                    "Valid Field",
                    "Description",
                    new BigDecimal("5.0"),
                    CropType.WHEAT,
                    SoilType.SANDY_LOAM,
                    IrrigationMethod.CANAL,
                    null,
                    null
            )).isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("owner ID must not be null");
        }

        @Test
        @DisplayName("Null area is rejected")
        void shouldRejectNullArea() {
            assertThatThrownBy(() -> Field.create(
                    ownerId,
                    "Valid Field",
                    "Description",
                    null,
                    CropType.WHEAT,
                    SoilType.SANDY_LOAM,
                    IrrigationMethod.CANAL,
                    null,
                    null
            )).isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("area in hectares must not be null");
        }

        @Test
        @DisplayName("Zero area is rejected")
        void shouldRejectZeroArea() {
            assertThatThrownBy(() -> Field.create(
                    ownerId,
                    "Valid Field",
                    "Description",
                    BigDecimal.ZERO,
                    CropType.WHEAT,
                    SoilType.SANDY_LOAM,
                    IrrigationMethod.CANAL,
                    null,
                    null
            )).isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("must be greater than zero");
        }

        @Test
        @DisplayName("Negative area is rejected")
        void shouldRejectNegativeArea() {
            assertThatThrownBy(() -> Field.create(
                    ownerId,
                    "Valid Field",
                    "Description",
                    new BigDecimal("-1.5"),
                    CropType.WHEAT,
                    SoilType.SANDY_LOAM,
                    IrrigationMethod.CANAL,
                    null,
                    null
            )).isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("must be greater than zero");
        }

        @Test
        @DisplayName("Null crop type is rejected")
        void shouldRejectNullCropType() {
            assertThatThrownBy(() -> Field.create(
                    ownerId,
                    "Valid Field",
                    "Description",
                    new BigDecimal("2.5"),
                    null,
                    SoilType.SANDY_LOAM,
                    IrrigationMethod.CANAL,
                    null,
                    null
            )).isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("crop type must not be null");
        }

        @Test
        @DisplayName("Null soil type is rejected")
        void shouldRejectNullSoilType() {
            assertThatThrownBy(() -> Field.create(
                    ownerId,
                    "Valid Field",
                    "Description",
                    new BigDecimal("2.5"),
                    CropType.COTTON,
                    null,
                    IrrigationMethod.CANAL,
                    null,
                    null
            )).isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("soil type must not be null");
        }

        @Test
        @DisplayName("Null irrigation method is rejected")
        void shouldRejectNullIrrigationMethod() {
            assertThatThrownBy(() -> Field.create(
                    ownerId,
                    "Valid Field",
                    "Description",
                    new BigDecimal("2.5"),
                    CropType.COTTON,
                    SoilType.LOAM,
                    null,
                    null,
                    null
            )).isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("irrigation method must not be null");
        }
    }

    @Nested
    @DisplayName("Location Validation Tests")
    class LocationTests {

        @Test
        @DisplayName("Valid coordinates succeed with 6 decimal places")
        void shouldAcceptValidLocation() {
            FieldLocation location = FieldLocation.of(41.4501234, 60.5901234);
            assertThat(location.latitude()).isEqualByComparingTo("41.450123");
            assertThat(location.longitude()).isEqualByComparingTo("60.590123");
        }

        @Test
        @DisplayName("Latitude outside [-90, 90] is rejected")
        void shouldRejectInvalidLatitude() {
            assertThatThrownBy(() -> FieldLocation.of(91.0, 60.5))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("between -90.0 and 90.0");

            assertThatThrownBy(() -> FieldLocation.of(-90.1, 60.5))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("between -90.0 and 90.0");
        }

        @Test
        @DisplayName("Longitude outside [-180, 180] is rejected")
        void shouldRejectInvalidLongitude() {
            assertThatThrownBy(() -> FieldLocation.of(41.5, 180.1))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("between -180.0 and 180.0");

            assertThatThrownBy(() -> FieldLocation.of(41.5, -180.1))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("between -180.0 and 180.0");
        }

        @Test
        @DisplayName("Providing only latitude without longitude is rejected")
        void shouldRejectPartialLocation() {
            assertThatThrownBy(() -> FieldLocation.of(new BigDecimal("41.5"), null))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("Both latitude and longitude must be provided together");
        }
    }

    @Nested
    @DisplayName("Boundary Validation Tests")
    class BoundaryTests {

        @Test
        @DisplayName("Auto-closing boundary from exterior ring works")
        void shouldAutoCloseExteriorRing() {
            List<CoordinatePoint> points = List.of(
                    CoordinatePoint.of(60.5900, 41.4500),
                    CoordinatePoint.of(60.5950, 41.4500),
                    CoordinatePoint.of(60.5950, 41.4550),
                    CoordinatePoint.of(60.5900, 41.4550)
            );
            FieldBoundary boundary = FieldBoundary.fromExteriorRing(points);

            assertThat(boundary.type()).isEqualTo("Polygon");
            assertThat(boundary.coordinates()).hasSize(1);
            List<List<Double>> exteriorRing = boundary.coordinates().getFirst();
            // 4 points + auto-closed point = 5 points
            assertThat(exteriorRing).hasSize(5);
            assertThat(exteriorRing.getFirst()).isEqualTo(exteriorRing.getLast());
        }

        @Test
        @DisplayName("Boundary with fewer than 3 points is rejected")
        void shouldRejectTooFewPoints() {
            List<CoordinatePoint> points = List.of(
                    CoordinatePoint.of(60.5900, 41.4500),
                    CoordinatePoint.of(60.5950, 41.4500)
            );
            assertThatThrownBy(() -> FieldBoundary.fromExteriorRing(points))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("At least 3 distinct coordinate points are required");
        }

        @Test
        @DisplayName("Unclosed boundary constructor is rejected")
        void shouldRejectUnclosedBoundary() {
            List<List<List<Double>>> rawCoords = List.of(List.of(
                    List.of(60.5900, 41.4500),
                    List.of(60.5950, 41.4500),
                    List.of(60.5950, 41.4550),
                    List.of(60.5900, 41.4590) // Different from first!
            ));
            assertThatThrownBy(() -> new FieldBoundary("Polygon", rawCoords))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("exterior ring must be closed");
        }

        @Test
        @DisplayName("Non-Polygon boundary type is rejected")
        void shouldRejectNonPolygonType() {
            List<List<List<Double>>> rawCoords = List.of(List.of(
                    List.of(60.5900, 41.4500),
                    List.of(60.5950, 41.4500),
                    List.of(60.5950, 41.4550),
                    List.of(60.5900, 41.4500)
            ));
            assertThatThrownBy(() -> new FieldBoundary("Point", rawCoords))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("boundary type must be 'Polygon'");
        }
    }

    @Nested
    @DisplayName("Lifecycle & Ownership Methods")
    class LifecycleTests {

        @Test
        @DisplayName("isOwnedBy returns true only for matching owner ID")
        void shouldVerifyOwnership() {
            Field field = createValidField();
            UUID randomUser = UUID.randomUUID();

            assertThat(field.isOwnedBy(ownerId)).isTrue();
            assertThat(field.isOwnedBy(randomUser)).isFalse();
            assertThat(field.isOwnedBy(null)).isFalse();
        }

        @Test
        @DisplayName("Deactivate and activate toggle status and update timestamp")
        void shouldToggleStatus() {
            Field field = createValidField();
            assertThat(field.isActive()).isTrue();

            field.deactivate();
            assertThat(field.getStatus()).isEqualTo(FieldStatus.INACTIVE);
            assertThat(field.isInactive()).isTrue();
            assertThat(field.isActive()).isFalse();

            field.activate();
            assertThat(field.getStatus()).isEqualTo(FieldStatus.ACTIVE);
            assertThat(field.isActive()).isTrue();
        }

        @Test
        @DisplayName("updateDetails modifies fields and preserves invariants")
        void shouldUpdateDetails() {
            Field field = createValidField();

            field.updateDetails(
                    "Updated Field Name",
                    "New description",
                    new BigDecimal("15.7500"),
                    CropType.RICE,
                    SoilType.CLAY,
                    IrrigationMethod.CANAL,
                    null,
                    null
            );

            assertThat(field.getName()).isEqualTo("Updated Field Name");
            assertThat(field.getDescription()).isEqualTo("New description");
            assertThat(field.getAreaHectares()).isEqualByComparingTo("15.7500");
            assertThat(field.getCropType()).isEqualTo(CropType.RICE);
            assertThat(field.getSoilType()).isEqualTo(SoilType.CLAY);
            assertThat(field.getIrrigationMethod()).isEqualTo(IrrigationMethod.CANAL);
            assertThat(field.getLocation()).isNull();
            assertThat(field.getBoundary()).isNull();
        }
    }
}
