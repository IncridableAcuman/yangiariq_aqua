-- liquibase formatted sql

-- changeset aquaai:018-create-anomaly-domain-tables
-- validCheckSum: 9:59ee3b6ba80b5b2a688aa4c72790f339
-- validCheckSum: 9:acd56682204e5d6d0bbd7a6481975d75
-- validCheckSum: ANY
-- comment: Milestone 08.0: Create tables for telemetry anomaly detection, evidence, and configurable rules

CREATE TABLE IF NOT EXISTS anomalies (
    id UUID PRIMARY KEY,
    field_id UUID NOT NULL REFERENCES fields(id) ON DELETE CASCADE,
    sensor_id UUID REFERENCES sensor_devices(id) ON DELETE SET NULL,
    sensor_type VARCHAR(32) NOT NULL,
    anomaly_type VARCHAR(64) NOT NULL,
    severity VARCHAR(32) NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'DETECTED',
    detected_value NUMERIC(14, 4),
    expected_value NUMERIC(14, 4),
    deviation NUMERIC(14, 4),
    confidence NUMERIC(5, 4) NOT NULL,
    is_simulated BOOLEAN NOT NULL DEFAULT FALSE,
    description VARCHAR(500) NOT NULL,
    explanation TEXT NOT NULL,
    source VARCHAR(64) NOT NULL DEFAULT 'RULE_ENGINE',
    observation_count INT NOT NULL DEFAULT 1,
    first_observed_at TIMESTAMP WITH TIME ZONE NOT NULL,
    last_observed_at TIMESTAMP WITH TIME ZONE NOT NULL,
    detected_at TIMESTAMP WITH TIME ZONE NOT NULL,
    acknowledged_at TIMESTAMP WITH TIME ZONE,
    acknowledged_by UUID,
    resolved_at TIMESTAMP WITH TIME ZONE,
    resolved_by UUID,
    resolution_notes VARCHAR(1000),
    last_notified_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_anomalies_field_status ON anomalies(field_id, status);
CREATE INDEX IF NOT EXISTS idx_anomalies_sensor_status ON anomalies(sensor_id, status);
CREATE INDEX IF NOT EXISTS idx_anomalies_severity ON anomalies(severity);
CREATE INDEX IF NOT EXISTS idx_anomalies_anomaly_type ON anomalies(anomaly_type);
CREATE INDEX IF NOT EXISTS idx_anomalies_detected_at ON anomalies(detected_at DESC);
CREATE INDEX IF NOT EXISTS idx_anomalies_active_dedup ON anomalies(field_id, sensor_id, anomaly_type, status);

-- changeset aquaai:018b-create-anomaly-postgresql-unique-index dbms:postgresql
-- validCheckSum: ANY
-- comment: PostgreSQL-specific unique partial index for active anomaly deduplication
CREATE UNIQUE INDEX IF NOT EXISTS uq_active_anomaly_dedup 
ON anomalies(field_id, COALESCE(sensor_id, '00000000-0000-0000-0000-000000000000'::uuid), anomaly_type) 
WHERE status IN ('DETECTED', 'ACKNOWLEDGED', 'INVESTIGATING');

-- changeset aquaai:018c-create-anomaly-evidence-and-rules
-- validCheckSum: ANY
-- comment: Evidence and configurable rule tables for anomaly detection

CREATE TABLE IF NOT EXISTS anomaly_evidence (
    id UUID PRIMARY KEY,
    anomaly_id UUID NOT NULL REFERENCES anomalies(id) ON DELETE CASCADE,
    evidence_type VARCHAR(64) NOT NULL,
    metric VARCHAR(64) NOT NULL,
    observed_value NUMERIC(14, 4),
    expected_value NUMERIC(14, 4),
    measured_at TIMESTAMP WITH TIME ZONE NOT NULL,
    metadata TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_anomaly_evidence_anomaly_id ON anomaly_evidence(anomaly_id);

CREATE TABLE IF NOT EXISTS anomaly_rule_configs (
    id UUID PRIMARY KEY,
    rule_code VARCHAR(64) NOT NULL UNIQUE,
    sensor_type VARCHAR(32) NOT NULL,
    name VARCHAR(128) NOT NULL,
    description VARCHAR(500),
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    severity VARCHAR(32) NOT NULL,
    threshold NUMERIC(14, 4) NOT NULL,
    secondary_threshold NUMERIC(14, 4),
    time_window_minutes INT NOT NULL DEFAULT 60,
    min_consecutive_readings INT NOT NULL DEFAULT 1,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_anomaly_rule_configs_sensor_type ON anomaly_rule_configs(sensor_type);

-- changeset aquaai:019-seed-default-anomaly-rules
-- validCheckSum: ANY
-- comment: Seed default configurable anomaly detection rules for agricultural sensor modalities

INSERT INTO anomaly_rule_configs (id, rule_code, sensor_type, name, description, enabled, severity, threshold, secondary_threshold, time_window_minutes, min_consecutive_readings, created_at, updated_at)
VALUES
('a1111111-1111-1111-1111-111111111101', 'RULE_SOIL_MOISTURE_DEFICIT', 'SOIL_MOISTURE', 'Tuproq namligi kritik kamayishi', 'Tuproq namligi 15% dan pastga tushganda o''ta xavfli qurg''oqchilik signali', TRUE, 'CRITICAL', 15.0000, 25.0000, 60, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('a1111111-1111-1111-1111-111111111102', 'RULE_WATER_FLOW_SPIKE', 'WATER_FLOW', 'Suv oqimi sakrashi va qochqin xavfi', 'Suv oqimi 45 m3/soat dan oshganda yuqori oqim ogohlantirishi', TRUE, 'HIGH', 45.0000, 30.0000, 30, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('a1111111-1111-1111-1111-111111111103', 'RULE_PRESSURE_DROP', 'PRESSURE', 'Gidravlik bosimning kritik pasayishi', 'Sug''orishda quvur bosimi 1.2 bar dan tushib ketganda ehtimoliy yorilish', TRUE, 'HIGH', 1.2000, 5.5000, 15, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('a1111111-1111-1111-1111-111111111104', 'RULE_SALINITY_HIGH', 'SALINITY', 'Tuproq / suv sho''rlanishi ortishi', 'Sho''rlanish 4.0 dS/m dan oshganda sho''r yuvish zarurati', TRUE, 'HIGH', 4.0000, 6.5000, 120, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('a1111111-1111-1111-1111-111111111105', 'RULE_TEMPERATURE_EXTREME', 'TEMPERATURE', 'Ekstremal harorat xavfi', 'Harorat 42C dan oshganda yoki 2C dan tushganda ayoz xavfi', TRUE, 'HIGH', 42.0000, 2.0000, 60, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('a1111111-1111-1111-1111-111111111106', 'RULE_HUMIDITY_LOW', 'HUMIDITY', 'Quruq garmsel namligi', 'Nisbiy namlik 15% dan tushganda bug''lanish jadallashishi', TRUE, 'MEDIUM', 15.0000, NULL, 60, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('a1111111-1111-1111-1111-111111111107', 'RULE_RAINFALL_HEAVY', 'RAINFALL', 'Jala va kuchli yog''ingarchilik', 'Yog''in miqdori soatiga 50 mm dan oshganda sel xavfi', TRUE, 'HIGH', 50.0000, NULL, 60, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
