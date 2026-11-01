-- liquibase formatted sql

-- changeset aqua-ai:015-create-sensor-devices-table
-- validCheckSum: ANY
-- comment: Create sensor_devices table for agricultural telemetry devices attached to field parcels
CREATE TABLE IF NOT EXISTS sensor_devices (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    field_id UUID NOT NULL,
    name VARCHAR(128) NOT NULL,
    device_identifier VARCHAR(64) NOT NULL,
    type VARCHAR(32) NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE',
    provider VARCHAR(64) NOT NULL DEFAULT 'SIMULATED',
    description VARCHAR(500),
    installed_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_seen_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT DEFAULT 0,
    CONSTRAINT fk_sensor_devices_field FOREIGN KEY (field_id) REFERENCES fields(id) ON DELETE RESTRICT,
    CONSTRAINT chk_sensor_device_status CHECK (status IN ('ACTIVE', 'INACTIVE', 'MAINTENANCE', 'OFFLINE')),
    CONSTRAINT chk_sensor_device_type CHECK (type IN ('SOIL_MOISTURE', 'WATER_FLOW', 'TEMPERATURE', 'HUMIDITY', 'WATER_LEVEL', 'PRESSURE', 'RAINFALL', 'SALINITY')),
    CONSTRAINT uq_sensor_device_identifier UNIQUE (device_identifier)
);

CREATE INDEX IF NOT EXISTS idx_sensor_devices_field_id ON sensor_devices(field_id);
CREATE INDEX IF NOT EXISTS idx_sensor_devices_status ON sensor_devices(status);
CREATE INDEX IF NOT EXISTS idx_sensor_devices_type ON sensor_devices(type);

-- changeset aqua-ai:016-create-sensor-readings-table
-- validCheckSum: 9:f2350597f21d012c7e7d1a3648a4c56e
-- validCheckSum: ANY
-- comment: Create sensor_readings table for time-series telemetry observations
CREATE TABLE IF NOT EXISTS sensor_readings (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    sensor_id UUID NOT NULL,
    field_id UUID NOT NULL,
    type VARCHAR(32) NOT NULL,
    "value" NUMERIC(14, 4) NOT NULL,
    unit VARCHAR(32) NOT NULL,
    quality VARCHAR(16) NOT NULL DEFAULT 'VALID',
    source VARCHAR(64) NOT NULL,
    measured_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_sensor_readings_sensor FOREIGN KEY (sensor_id) REFERENCES sensor_devices(id) ON DELETE CASCADE,
    CONSTRAINT fk_sensor_readings_field FOREIGN KEY (field_id) REFERENCES fields(id) ON DELETE RESTRICT,
    CONSTRAINT chk_sensor_reading_quality CHECK (quality IN ('VALID', 'SUSPECT', 'INVALID')),
    CONSTRAINT chk_sensor_reading_type CHECK (type IN ('SOIL_MOISTURE', 'WATER_FLOW', 'TEMPERATURE', 'HUMIDITY', 'WATER_LEVEL', 'PRESSURE', 'RAINFALL', 'SALINITY'))
);

CREATE INDEX IF NOT EXISTS idx_sensor_readings_sensor_measured ON sensor_readings(sensor_id, measured_at DESC);
CREATE INDEX IF NOT EXISTS idx_sensor_readings_field_measured ON sensor_readings(field_id, measured_at DESC);
CREATE INDEX IF NOT EXISTS idx_sensor_readings_field_type_measured ON sensor_readings(field_id, type, measured_at DESC);
CREATE INDEX IF NOT EXISTS idx_sensor_readings_measured_at ON sensor_readings(measured_at DESC);
