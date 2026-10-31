-- liquibase formatted sql

-- changeset aqua-ai:014-create-water-consumption-aggregation-indexes
-- comment: Composite indexes for high-throughput water consumption aggregations, plan metrics, and field summaries
CREATE INDEX IF NOT EXISTS idx_irrigation_events_plan_status ON irrigation_events(irrigation_plan_id, status);
CREATE INDEX IF NOT EXISTS idx_irrigation_events_field_status ON irrigation_events(field_id, status);
CREATE INDEX IF NOT EXISTS idx_irrigation_events_field_started ON irrigation_events(field_id, started_at);
CREATE INDEX IF NOT EXISTS idx_water_allocations_field_status ON water_allocations(field_id, status);
