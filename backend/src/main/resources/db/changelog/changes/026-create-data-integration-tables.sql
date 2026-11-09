-- liquibase formatted sql

-- changeset aqua-ai:044-create-data-sources-table
-- validCheckSum: ANY
-- comment: Milestone 08.19: Create data_sources registry table
CREATE TABLE IF NOT EXISTS data_sources (
    id UUID PRIMARY KEY,
    source_code VARCHAR(64) NOT NULL UNIQUE,
    name VARCHAR(128) NOT NULL,
    source_type VARCHAR(32) NOT NULL,
    provider_name VARCHAR(64) NOT NULL,
    description VARCHAR(500),
    status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE',
    endpoint_url VARCHAR(255),
    configuration_ref VARCHAR(128),
    last_sync_at TIMESTAMP WITH TIME ZONE,
    last_success_at TIMESTAMP WITH TIME ZONE,
    last_error_at TIMESTAMP WITH TIME ZONE,
    last_error_message VARCHAR(1000),
    total_ingested_count BIGINT NOT NULL DEFAULT 0,
    total_rejected_count BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_data_source_type CHECK (source_type IN (
        'SENSOR_DEVICE', 'WEATHER_PROVIDER', 'OPERATOR_INPUT', 'FARMER_INPUT',
        'CSV_IMPORT', 'EXTERNAL_API', 'MANUAL_MEASUREMENT', 'SIMULATED_PROVIDER'
    )),
    CONSTRAINT chk_data_source_status CHECK (status IN (
        'ACTIVE', 'INACTIVE', 'DEGRADED', 'NOT_CONFIGURED', 'DISCONNECTED', 'ERROR'
    ))
);

CREATE INDEX IF NOT EXISTS idx_data_sources_status ON data_sources(status);
CREATE INDEX IF NOT EXISTS idx_data_sources_type ON data_sources(source_type);

-- changeset aqua-ai:045-create-ingested-provenance-table
-- validCheckSum: ANY
-- comment: Milestone 08.19: Create ingested_provenance table for telemetry traceability and idempotency
CREATE TABLE IF NOT EXISTS ingested_provenance (
    id UUID PRIMARY KEY,
    data_source_id UUID NOT NULL,
    external_record_id VARCHAR(128),
    idempotency_key VARCHAR(160) NOT NULL UNIQUE,
    sensor_reading_id UUID,
    field_id UUID NOT NULL,
    sensor_id UUID,
    sensor_type VARCHAR(32) NOT NULL,
    raw_value NUMERIC(14, 4) NOT NULL,
    raw_unit VARCHAR(32) NOT NULL,
    normalized_value NUMERIC(14, 4) NOT NULL,
    canonical_unit VARCHAR(32) NOT NULL,
    quality_status VARCHAR(32) NOT NULL,
    evidence_classification VARCHAR(32) NOT NULL,
    validation_message VARCHAR(500),
    measured_at TIMESTAMP WITH TIME ZONE NOT NULL,
    ingested_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_provenance_source FOREIGN KEY (data_source_id) REFERENCES data_sources(id) ON DELETE RESTRICT,
    CONSTRAINT fk_provenance_reading FOREIGN KEY (sensor_reading_id) REFERENCES sensor_readings(id) ON DELETE SET NULL,
    CONSTRAINT fk_provenance_field FOREIGN KEY (field_id) REFERENCES fields(id) ON DELETE RESTRICT,
    CONSTRAINT fk_provenance_sensor FOREIGN KEY (sensor_id) REFERENCES sensor_devices(id) ON DELETE SET NULL,
    CONSTRAINT chk_provenance_quality CHECK (quality_status IN (
        'VALID', 'INVALID', 'STALE', 'DUPLICATE', 'INCOMPLETE', 'OUT_OF_RANGE', 'UNVERIFIED'
    )),
    CONSTRAINT chk_provenance_evidence CHECK (evidence_classification IN (
        'OBSERVED', 'VERIFIED', 'ESTIMATED', 'CALCULATED', 'SIMULATED', 'MISSING'
    ))
);

CREATE INDEX IF NOT EXISTS idx_provenance_source_ingested ON ingested_provenance(data_source_id, ingested_at DESC);
CREATE INDEX IF NOT EXISTS idx_provenance_field_measured ON ingested_provenance(field_id, measured_at DESC);
CREATE INDEX IF NOT EXISTS idx_provenance_quality ON ingested_provenance(quality_status);
CREATE INDEX IF NOT EXISTS idx_provenance_evidence ON ingested_provenance(evidence_classification);

-- changeset aqua-ai:046-create-import-batches-table
-- validCheckSum: ANY
-- comment: Milestone 08.19: Create import_batches and import_row_results tables for CSV imports
CREATE TABLE IF NOT EXISTS import_batches (
    id UUID PRIMARY KEY,
    data_source_id UUID NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    file_size_bytes BIGINT NOT NULL,
    total_rows INT NOT NULL DEFAULT 0,
    successful_rows INT NOT NULL DEFAULT 0,
    rejected_rows INT NOT NULL DEFAULT 0,
    status VARCHAR(32) NOT NULL DEFAULT 'PENDING',
    uploaded_by UUID,
    started_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT fk_import_batch_source FOREIGN KEY (data_source_id) REFERENCES data_sources(id) ON DELETE RESTRICT,
    CONSTRAINT fk_import_batch_user FOREIGN KEY (uploaded_by) REFERENCES users(id) ON DELETE SET NULL,
    CONSTRAINT chk_import_batch_status CHECK (status IN ('PENDING', 'COMPLETED', 'FAILED', 'PARTIAL'))
);

CREATE TABLE IF NOT EXISTS import_row_results (
    id UUID PRIMARY KEY,
    batch_id UUID NOT NULL,
    row_number INT NOT NULL,
    is_success BOOLEAN NOT NULL,
    external_record_id VARCHAR(128),
    error_message VARCHAR(500),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_import_row_batch FOREIGN KEY (batch_id) REFERENCES import_batches(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_import_rows_batch ON import_row_results(batch_id, row_number);

-- changeset aqua-ai:047-seed-initial-data-sources
-- validCheckSum: ANY
-- comment: Milestone 08.19: Seed default system data sources
INSERT INTO data_sources (
    id, source_code, name, source_type, provider_name, description, status, configuration_ref
) VALUES 
    ('10000000-0000-0000-0000-000000000001', 'SIMULATED_SENSOR_PROVIDER', 'Simulated Edge Telemetry', 'SIMULATED_PROVIDER', 'SimulatedSensorProvider', 'Internal bounded random-walk telemetry generator for testing and demonstration', 'ACTIVE', 'CONFIG:app.sensor.provider=simulated'),
    ('10000000-0000-0000-0000-000000000002', 'SIMULATED_YANGIARIQ_WEATHER', 'Simulated Yangiariq Agro-Meteorology', 'WEATHER_PROVIDER', 'SimulatedYangiariqWeatherProvider', 'Seasonal climate model for Yangiariq district, Khorezm', 'ACTIVE', 'INTERNAL:weather.simulated'),
    ('10000000-0000-0000-0000-000000000003', 'OPEN_METEO_WEATHER', 'Open-Meteo Regional Weather API', 'WEATHER_PROVIDER', 'OpenMeteoWeatherAdapter', 'Automated forecast and observation integration via Open-Meteo API', 'NOT_CONFIGURED', 'ENV:OPEN_METEO_API_URL'),
    ('10000000-0000-0000-0000-000000000004', 'CSV_TELEMETRY_IMPORT', 'CSV Telemetry Ingestion Gateway', 'CSV_IMPORT', 'CsvTelemetryImporter', 'Controlled batch file ingestion for historical or field-collected sensor readings', 'ACTIVE', 'FILE:multipart/form-data'),
    ('10000000-0000-0000-0000-000000000005', 'OPERATOR_MANUAL_INPUT', 'Operator Field Verification Console', 'OPERATOR_INPUT', 'ManualMeasurementService', 'Direct operator-entered verified or observed physical field measurements', 'ACTIVE', 'REST:manual-entry'),
    ('10000000-0000-0000-0000-000000000006', 'HTTP_SENSOR_GATEWAY', 'HTTP IoT Edge Telemetry Receiver', 'SENSOR_DEVICE', 'HttpSensorGatewayAdapter', 'Webhook receiver for LoRaWAN / 4G cellular IoT telemetry edge gateways', 'NOT_CONFIGURED', 'ENV:SENSOR_GATEWAY_TOKEN');
