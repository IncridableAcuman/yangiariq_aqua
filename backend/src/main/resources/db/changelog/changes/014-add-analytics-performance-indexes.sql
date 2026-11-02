-- liquibase formatted sql

-- changeset aqua-ai:031-create-analytics-performance-indexes
-- comment: Milestone 08.6: Composite indexes to optimize farm performance analytics and chronological projections
CREATE INDEX IF NOT EXISTS idx_irr_events_field_started_status ON irrigation_events(field_id, started_at, status);
CREATE INDEX IF NOT EXISTS idx_anomalies_field_detected_severity ON anomalies(field_id, detected_at, severity);
CREATE INDEX IF NOT EXISTS idx_sensor_readings_field_time_quality ON sensor_readings(field_id, measured_at, quality);
CREATE INDEX IF NOT EXISTS idx_smart_plans_field_date_status ON smart_irrigation_plans(field_id, planned_date, status);
