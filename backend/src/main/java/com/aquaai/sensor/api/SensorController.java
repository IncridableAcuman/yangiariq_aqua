package com.aquaai.sensor.api;

import com.aquaai.sensor.api.dto.CreateSensorRequest;
import com.aquaai.sensor.api.dto.FieldTelemetrySummaryResponse;
import com.aquaai.sensor.api.dto.RecordSensorReadingRequest;
import com.aquaai.sensor.api.dto.SensorReadingPageResponse;
import com.aquaai.sensor.api.dto.SensorReadingResponse;
import com.aquaai.sensor.api.dto.SensorResponse;
import com.aquaai.sensor.api.dto.UpdateSensorRequest;
import com.aquaai.sensor.application.service.SensorSimulationService;
import com.aquaai.sensor.application.usecase.ActivateSensorUseCase;
import com.aquaai.sensor.application.usecase.CreateSensorUseCase;
import com.aquaai.sensor.application.usecase.DeactivateSensorUseCase;
import com.aquaai.sensor.application.usecase.GetFieldTelemetrySummaryUseCase;
import com.aquaai.sensor.application.usecase.GetLatestSensorReadingUseCase;
import com.aquaai.sensor.application.usecase.GetSensorUseCase;
import com.aquaai.sensor.application.usecase.ListFieldReadingsUseCase;
import com.aquaai.sensor.application.usecase.ListFieldSensorsUseCase;
import com.aquaai.sensor.application.usecase.ListSensorReadingsUseCase;
import com.aquaai.sensor.application.usecase.RecordSensorReadingUseCase;
import com.aquaai.sensor.application.usecase.SetSensorMaintenanceUseCase;
import com.aquaai.sensor.application.usecase.UpdateSensorUseCase;
import com.aquaai.sensor.domain.model.Sensor;
import com.aquaai.sensor.domain.model.SensorReading;
import com.aquaai.sensor.domain.model.SensorReadingPage;
import com.aquaai.shared.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * REST controller exposing agricultural sensor device management and time-series telemetry endpoints.
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Sensors & Telemetry", description = "Endpoints for managing field sensors, observing real-time telemetry, and running simulation models")
@SecurityRequirement(name = "bearerAuth")
public class SensorController {

    private final CreateSensorUseCase createSensorUseCase;
    private final GetSensorUseCase getSensorUseCase;
    private final ListFieldSensorsUseCase listFieldSensorsUseCase;
    private final UpdateSensorUseCase updateSensorUseCase;
    private final ActivateSensorUseCase activateSensorUseCase;
    private final DeactivateSensorUseCase deactivateSensorUseCase;
    private final SetSensorMaintenanceUseCase setSensorMaintenanceUseCase;

    private final RecordSensorReadingUseCase recordSensorReadingUseCase;
    private final GetLatestSensorReadingUseCase getLatestSensorReadingUseCase;
    private final ListSensorReadingsUseCase listSensorReadingsUseCase;
    private final ListFieldReadingsUseCase listFieldReadingsUseCase;
    private final GetFieldTelemetrySummaryUseCase getFieldTelemetrySummaryUseCase;
    private final SensorSimulationService sensorSimulationService;

    // ------------------------------------------------------------------------
    // Sensor Aggregate Lifecycle Endpoints
    // ------------------------------------------------------------------------

    @PostMapping("/sensors")
    @Operation(summary = "Register and deploy a new sensor", description = "Assigns a physical or simulated telemetry device to a specified agricultural parcel")
    public ResponseEntity<ApiResponse<SensorResponse>> createSensor(
            @Valid @RequestBody CreateSensorRequest request
    ) {
        CreateSensorUseCase.Command command = new CreateSensorUseCase.Command(
                request.fieldId(),
                request.name(),
                request.deviceIdentifier(),
                request.type(),
                request.provider(),
                request.description()
        );
        Sensor created = createSensorUseCase.execute(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(SensorResponse.from(created)));
    }

    @GetMapping("/sensors/{id}")
    @Operation(summary = "Get sensor details", description = "Retrieves sensor device metadata by unique ID (enforces anti-IDOR parcel ownership)")
    public ResponseEntity<ApiResponse<SensorResponse>> getSensorById(
            @PathVariable UUID id
    ) {
        Sensor sensor = getSensorUseCase.execute(id);
        return ResponseEntity.ok(ApiResponse.ok(SensorResponse.from(sensor)));
    }

    @PatchMapping("/sensors/{id}")
    @Operation(summary = "Update sensor details", description = "Updates mutable metadata (name, description)")
    public ResponseEntity<ApiResponse<SensorResponse>> updateSensor(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateSensorRequest request
    ) {
        UpdateSensorUseCase.Command command = new UpdateSensorUseCase.Command(
                request.name(),
                request.description()
        );
        Sensor updated = updateSensorUseCase.execute(id, command);
        return ResponseEntity.ok(ApiResponse.ok(SensorResponse.from(updated)));
    }

    @PatchMapping("/sensors/{id}/activate")
    @Operation(summary = "Activate sensor", description = "Transitions sensor lifecycle status to ACTIVE")
    public ResponseEntity<ApiResponse<SensorResponse>> activateSensor(
            @PathVariable UUID id
    ) {
        Sensor updated = activateSensorUseCase.execute(id);
        return ResponseEntity.ok(ApiResponse.ok(SensorResponse.from(updated)));
    }

    @PatchMapping("/sensors/{id}/deactivate")
    @Operation(summary = "Deactivate sensor", description = "Transitions sensor lifecycle status to INACTIVE")
    public ResponseEntity<ApiResponse<SensorResponse>> deactivateSensor(
            @PathVariable UUID id
    ) {
        Sensor updated = deactivateSensorUseCase.execute(id);
        return ResponseEntity.ok(ApiResponse.ok(SensorResponse.from(updated)));
    }

    @PatchMapping("/sensors/{id}/maintenance")
    @Operation(summary = "Set sensor to maintenance", description = "Transitions sensor lifecycle status to MAINTENANCE")
    public ResponseEntity<ApiResponse<SensorResponse>> setSensorMaintenance(
            @PathVariable UUID id
    ) {
        Sensor updated = setSensorMaintenanceUseCase.execute(id);
        return ResponseEntity.ok(ApiResponse.ok(SensorResponse.from(updated)));
    }

    @GetMapping("/fields/{fieldId}/sensors")
    @Operation(summary = "List sensors on a field", description = "Returns all telemetry sensors assigned to a parcel")
    public ResponseEntity<ApiResponse<List<SensorResponse>>> listFieldSensors(
            @PathVariable UUID fieldId
    ) {
        List<Sensor> sensors = listFieldSensorsUseCase.execute(fieldId);
        List<SensorResponse> response = sensors.stream().map(SensorResponse::from).toList();
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    // ------------------------------------------------------------------------
    // Telemetry Observations & Reading Endpoints
    // ------------------------------------------------------------------------

    @PostMapping("/sensors/{id}/readings")
    @Operation(summary = "Record a telemetry reading", description = "Submits a validated instantaneous telemetry observation for a sensor")
    public ResponseEntity<ApiResponse<SensorReadingResponse>> recordReading(
            @PathVariable UUID id,
            @Valid @RequestBody RecordSensorReadingRequest request
    ) {
        RecordSensorReadingUseCase.Command command = new RecordSensorReadingUseCase.Command(
                id,
                request.value(),
                request.unit(),
                request.quality(),
                request.source(),
                request.measuredAt()
        );
        SensorReading reading = recordSensorReadingUseCase.execute(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(SensorReadingResponse.from(reading)));
    }

    @GetMapping("/sensors/{id}/latest-reading")
    @Operation(summary = "Get latest reading for sensor", description = "Retrieves the most recent telemetry observation recorded for a sensor")
    public ResponseEntity<ApiResponse<SensorReadingResponse>> getLatestReading(
            @PathVariable UUID id
    ) {
        SensorReading reading = getLatestSensorReadingUseCase.execute(id);
        return ResponseEntity.ok(ApiResponse.ok(SensorReadingResponse.from(reading)));
    }

    @GetMapping("/sensors/{id}/readings")
    @Operation(summary = "List sensor reading history", description = "Retrieves paginated historical telemetry observations for a sensor with optional time filtering")
    public ResponseEntity<ApiResponse<SensorReadingPageResponse>> listSensorReadings(
            @PathVariable UUID id,
            @Parameter(description = "Start instant in ISO-8601 UTC format")
            @RequestParam(required = false) Instant from,
            @Parameter(description = "End instant in ISO-8601 UTC format")
            @RequestParam(required = false) Instant to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "desc") String sort
    ) {
        ListSensorReadingsUseCase.Query query = new ListSensorReadingsUseCase.Query(
                id,
                from,
                to,
                page,
                size,
                sort
        );
        SensorReadingPage domainPage = listSensorReadingsUseCase.execute(query);
        return ResponseEntity.ok(ApiResponse.ok(SensorReadingPageResponse.from(domainPage)));
    }

    @GetMapping("/fields/{fieldId}/readings")
    @Operation(summary = "List field reading history", description = "Retrieves paginated historical telemetry observations across all sensors on a field")
    public ResponseEntity<ApiResponse<SensorReadingPageResponse>> listFieldReadings(
            @PathVariable UUID fieldId,
            @Parameter(description = "Start instant in ISO-8601 UTC format")
            @RequestParam(required = false) Instant from,
            @Parameter(description = "End instant in ISO-8601 UTC format")
            @RequestParam(required = false) Instant to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "desc") String sort
    ) {
        ListFieldReadingsUseCase.Query query = new ListFieldReadingsUseCase.Query(
                fieldId,
                from,
                to,
                page,
                size,
                sort
        );
        SensorReadingPage domainPage = listFieldReadingsUseCase.execute(query);
        return ResponseEntity.ok(ApiResponse.ok(SensorReadingPageResponse.from(domainPage)));
    }

    @GetMapping("/fields/{fieldId}/readings/latest-summary")
    @Operation(summary = "Get field telemetry summary", description = "Single efficient query returning the latest telemetry reading for each sensor type deployed on a field")
    public ResponseEntity<ApiResponse<FieldTelemetrySummaryResponse>> getFieldTelemetrySummary(
            @PathVariable UUID fieldId
    ) {
        List<SensorReading> readings = getFieldTelemetrySummaryUseCase.execute(fieldId);
        List<SensorReadingResponse> dtos = readings.stream().map(SensorReadingResponse::from).toList();
        FieldTelemetrySummaryResponse summary = new FieldTelemetrySummaryResponse(fieldId, dtos, Instant.now());
        return ResponseEntity.ok(ApiResponse.ok(summary));
    }

    @PostMapping("/sensors/{id}/simulate")
    @Operation(summary = "Trigger on-demand simulation", description = "Generates and records an immediate bounded simulation reading for demonstration or testing")
    public ResponseEntity<ApiResponse<SensorReadingResponse>> simulateReading(
            @PathVariable UUID id
    ) {
        SensorReading reading = sensorSimulationService.simulateReadingForSensor(id);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(SensorReadingResponse.from(reading)));
    }
}
