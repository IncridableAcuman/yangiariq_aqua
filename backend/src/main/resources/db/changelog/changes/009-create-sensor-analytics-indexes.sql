-- liquibase formatted sql

-- changeset aqua-ai:017-create-sensor-analytics-indexes
-- validCheckSum: ANY
-- comment: Add composite indexes for time-series analytics, quality filtering, and sensor availability sweeps
CREATE INDEX IF NOT EXISTS idx_sensor_readings_sensor_qual_time 
    ON sensor_readings(sensor_id, quality, measured_at DESC);

CREATE INDEX IF NOT EXISTS idx_sensor_readings_field_type_qual_time 
    ON sensor_readings(field_id, type, quality, measured_at DESC);

CREATE INDEX IF NOT EXISTS idx_sensor_devices_field_status 
    ON sensor_devices(field_id, status);

CREATE INDEX IF NOT EXISTS idx_sensor_devices_last_seen 
    ON sensor_devices(last_seen_at);
