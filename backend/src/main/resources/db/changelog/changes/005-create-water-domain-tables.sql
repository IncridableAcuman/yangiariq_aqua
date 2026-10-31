-- liquibase formatted sql

-- changeset aqua-ai:009-create-water-sources-table
-- comment: Create water_sources table for regional distribution infrastructure (canals, ditches, reservoirs, wells)
CREATE TABLE IF NOT EXISTS water_sources (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    name VARCHAR(128) NOT NULL,
    type VARCHAR(32) NOT NULL,
    description VARCHAR(500),
    status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT DEFAULT 0,
    CONSTRAINT chk_water_source_status CHECK (status IN ('ACTIVE', 'INACTIVE')),
    CONSTRAINT chk_water_source_type CHECK (type IN ('CANAL', 'DITCH', 'RESERVOIR', 'WELL', 'RIVER', 'OTHER'))
);

CREATE INDEX IF NOT EXISTS idx_water_sources_status ON water_sources(status);
CREATE INDEX IF NOT EXISTS idx_water_sources_type ON water_sources(type);

-- changeset aqua-ai:010-create-water-sources-unique-active-name-index dbms:postgresql
-- comment: Prevent duplicate active water source names on PostgreSQL
CREATE UNIQUE INDEX IF NOT EXISTS idx_water_sources_active_name ON water_sources(LOWER(name)) WHERE status = 'ACTIVE';

-- changeset aqua-ai:011-create-water-allocations-table
-- comment: Create water_allocations table linking parcel fields to water source volume quotas
CREATE TABLE IF NOT EXISTS water_allocations (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    field_id UUID NOT NULL,
    water_source_id UUID NOT NULL,
    allocated_volume_m3 NUMERIC(14, 4) NOT NULL,
    period_start TIMESTAMP WITH TIME ZONE NOT NULL,
    period_end TIMESTAMP WITH TIME ZONE NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE',
    notes VARCHAR(500),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT DEFAULT 0,
    CONSTRAINT fk_water_allocations_field FOREIGN KEY (field_id) REFERENCES fields(id) ON DELETE RESTRICT,
    CONSTRAINT fk_water_allocations_source FOREIGN KEY (water_source_id) REFERENCES water_sources(id) ON DELETE RESTRICT,
    CONSTRAINT chk_water_allocation_volume CHECK (allocated_volume_m3 > 0),
    CONSTRAINT chk_water_allocation_period CHECK (period_start <= period_end),
    CONSTRAINT chk_water_allocation_status CHECK (status IN ('ACTIVE', 'CLOSED', 'CANCELLED'))
);

CREATE INDEX IF NOT EXISTS idx_water_allocations_field ON water_allocations(field_id);
CREATE INDEX IF NOT EXISTS idx_water_allocations_source ON water_allocations(water_source_id);
CREATE INDEX IF NOT EXISTS idx_water_allocations_status ON water_allocations(status);
CREATE INDEX IF NOT EXISTS idx_water_allocations_period ON water_allocations(period_start, period_end);
CREATE INDEX IF NOT EXISTS idx_water_allocations_period_start ON water_allocations(period_start);

-- changeset aqua-ai:012-create-irrigation-plans-table
-- comment: Create irrigation_plans table representing scheduled parcel watering events
CREATE TABLE IF NOT EXISTS irrigation_plans (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    field_id UUID NOT NULL,
    water_allocation_id UUID,
    planned_start_at TIMESTAMP WITH TIME ZONE NOT NULL,
    planned_end_at TIMESTAMP WITH TIME ZONE NOT NULL,
    planned_volume_m3 NUMERIC(14, 4) NOT NULL,
    method VARCHAR(32) NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'PLANNED',
    notes VARCHAR(500),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT DEFAULT 0,
    CONSTRAINT fk_irrigation_plans_field FOREIGN KEY (field_id) REFERENCES fields(id) ON DELETE RESTRICT,
    CONSTRAINT fk_irrigation_plans_allocation FOREIGN KEY (water_allocation_id) REFERENCES water_allocations(id) ON DELETE SET NULL,
    CONSTRAINT chk_irrigation_plan_volume CHECK (planned_volume_m3 > 0),
    CONSTRAINT chk_irrigation_plan_period CHECK (planned_start_at < planned_end_at),
    CONSTRAINT chk_irrigation_plan_method CHECK (method IN ('CANAL', 'DITCH', 'FURROW', 'DRIP', 'SPRINKLER', 'OTHER')),
    CONSTRAINT chk_irrigation_plan_status CHECK (status IN ('PLANNED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED'))
);

CREATE INDEX IF NOT EXISTS idx_irrigation_plans_field ON irrigation_plans(field_id);
CREATE INDEX IF NOT EXISTS idx_irrigation_plans_allocation ON irrigation_plans(water_allocation_id);
CREATE INDEX IF NOT EXISTS idx_irrigation_plans_status ON irrigation_plans(status);
CREATE INDEX IF NOT EXISTS idx_irrigation_plans_period ON irrigation_plans(planned_start_at, planned_end_at);
CREATE INDEX IF NOT EXISTS idx_irrigation_plans_planned_start_at ON irrigation_plans(planned_start_at);

-- changeset aqua-ai:013-create-irrigation-events-table
-- comment: Create irrigation_events table capturing executed watering activities and actual consumption
CREATE TABLE IF NOT EXISTS irrigation_events (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    field_id UUID NOT NULL,
    irrigation_plan_id UUID,
    started_at TIMESTAMP WITH TIME ZONE NOT NULL,
    ended_at TIMESTAMP WITH TIME ZONE,
    actual_volume_m3 NUMERIC(14, 4),
    status VARCHAR(32) NOT NULL DEFAULT 'STARTED',
    notes VARCHAR(500),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT DEFAULT 0,
    CONSTRAINT fk_irrigation_events_field FOREIGN KEY (field_id) REFERENCES fields(id) ON DELETE RESTRICT,
    CONSTRAINT fk_irrigation_events_plan FOREIGN KEY (irrigation_plan_id) REFERENCES irrigation_plans(id) ON DELETE SET NULL,
    CONSTRAINT chk_irrigation_event_volume CHECK (actual_volume_m3 IS NULL OR actual_volume_m3 >= 0),
    CONSTRAINT chk_irrigation_event_period CHECK (ended_at IS NULL OR started_at < ended_at),
    CONSTRAINT chk_irrigation_event_status CHECK (status IN ('STARTED', 'COMPLETED', 'CANCELLED'))
);

CREATE INDEX IF NOT EXISTS idx_irrigation_events_field ON irrigation_events(field_id);
CREATE INDEX IF NOT EXISTS idx_irrigation_events_plan ON irrigation_events(irrigation_plan_id);
CREATE INDEX IF NOT EXISTS idx_irrigation_events_status ON irrigation_events(status);
CREATE INDEX IF NOT EXISTS idx_irrigation_events_started_at ON irrigation_events(started_at);
