-- liquibase formatted sql

-- changeset aqua-ai:026-create-smart-irrigation-plans-table
-- comment: Create smart_irrigation_plans table for AI-optimized and constraint-checked parcel watering schedules
CREATE TABLE IF NOT EXISTS smart_irrigation_plans (
    id UUID PRIMARY KEY,
    field_id UUID NOT NULL,
    water_allocation_id UUID,
    status VARCHAR(32) NOT NULL DEFAULT 'DRAFT',
    priority VARCHAR(32) NOT NULL DEFAULT 'MEDIUM',
    planned_date DATE NOT NULL,
    planned_start_at TIMESTAMP WITH TIME ZONE NOT NULL,
    planned_end_at TIMESTAMP WITH TIME ZONE NOT NULL,
    recommended_volume_m3 NUMERIC(14, 4) NOT NULL,
    estimated_duration_minutes INT NOT NULL,
    recommended_flow_rate_m3_h NUMERIC(10, 2),
    target_moisture_percent NUMERIC(5, 2),
    current_moisture_percent NUMERIC(5, 2),
    confidence_level VARCHAR(32) NOT NULL DEFAULT 'MEDIUM',
    confidence_score NUMERIC(5, 4),
    reason TEXT NOT NULL,
    reasoning_summary TEXT,
    cancellation_reason VARCHAR(500),
    is_blocked BOOLEAN NOT NULL DEFAULT FALSE,
    blocked_reason VARCHAR(500),
    approved_by UUID,
    approved_at TIMESTAMP WITH TIME ZONE,
    source VARCHAR(64) NOT NULL DEFAULT 'DETERMINISTIC_SCHEDULER',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT DEFAULT 0,
    CONSTRAINT fk_smart_irrigation_field FOREIGN KEY (field_id) REFERENCES fields(id) ON DELETE RESTRICT,
    CONSTRAINT fk_smart_irrigation_allocation FOREIGN KEY (water_allocation_id) REFERENCES water_allocations(id) ON DELETE SET NULL,
    CONSTRAINT fk_smart_irrigation_approver FOREIGN KEY (approved_by) REFERENCES users(id) ON DELETE SET NULL,
    CONSTRAINT chk_smart_irrigation_volume CHECK (recommended_volume_m3 >= 0),
    CONSTRAINT chk_smart_irrigation_period CHECK (planned_start_at < planned_end_at)
);

CREATE INDEX IF NOT EXISTS idx_smart_irrigation_field ON smart_irrigation_plans(field_id);
CREATE INDEX IF NOT EXISTS idx_smart_irrigation_status ON smart_irrigation_plans(status);
CREATE INDEX IF NOT EXISTS idx_smart_irrigation_planned_date ON smart_irrigation_plans(planned_date);
CREATE INDEX IF NOT EXISTS idx_smart_irrigation_field_status ON smart_irrigation_plans(field_id, status);
CREATE INDEX IF NOT EXISTS idx_smart_irrigation_field_date ON smart_irrigation_plans(field_id, planned_date);

-- changeset aqua-ai:027-create-smart-irrigation-plan-constraints-table
-- comment: Create smart_irrigation_plan_constraints table capturing evaluated constraint results per plan
CREATE TABLE IF NOT EXISTS smart_irrigation_plan_constraints (
    id UUID PRIMARY KEY,
    plan_id UUID NOT NULL,
    constraint_type VARCHAR(64) NOT NULL,
    status VARCHAR(32) NOT NULL,
    message VARCHAR(500) NOT NULL,
    details TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_plan_constraints_plan FOREIGN KEY (plan_id) REFERENCES smart_irrigation_plans(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_plan_constraints_plan ON smart_irrigation_plan_constraints(plan_id);

-- changeset aqua-ai:028-create-smart-irrigation-plan-tradeoffs-table
-- comment: Create smart_irrigation_plan_tradeoffs table capturing agronomic and scheduling trade-off explanations
CREATE TABLE IF NOT EXISTS smart_irrigation_plan_tradeoffs (
    id UUID PRIMARY KEY,
    plan_id UUID NOT NULL,
    category VARCHAR(64) NOT NULL,
    description TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_plan_tradeoffs_plan FOREIGN KEY (plan_id) REFERENCES smart_irrigation_plans(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_plan_tradeoffs_plan ON smart_irrigation_plan_tradeoffs(plan_id);
