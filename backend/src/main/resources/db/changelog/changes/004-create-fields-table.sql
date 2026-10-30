-- liquibase formatted sql

-- changeset aqua-ai:006-create-fields-table
-- comment: Create foundational fields table for agricultural parcels and boundaries
CREATE TABLE IF NOT EXISTS fields (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    owner_id UUID NOT NULL,
    name VARCHAR(128) NOT NULL,
    description VARCHAR(500),
    area_hectares NUMERIC(10, 4) NOT NULL,
    crop_type VARCHAR(32) NOT NULL,
    soil_type VARCHAR(32) NOT NULL,
    irrigation_method VARCHAR(32) NOT NULL,
    latitude NUMERIC(9, 6),
    longitude NUMERIC(9, 6),
    boundary JSONB,
    status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT DEFAULT 0,
    CONSTRAINT fk_fields_owner FOREIGN KEY (owner_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT chk_field_area_positive CHECK (area_hectares > 0),
    CONSTRAINT chk_field_latitude CHECK (latitude IS NULL OR (latitude >= -90.0 AND latitude <= 90.0)),
    CONSTRAINT chk_field_longitude CHECK (longitude IS NULL OR (longitude >= -180.0 AND longitude <= 180.0))
);

-- changeset aqua-ai:007-create-fields-indexes
-- comment: Index owner_id for high-frequency farmer field queries and status for active parcel filtering
CREATE INDEX IF NOT EXISTS idx_fields_owner_id ON fields(owner_id);
CREATE INDEX IF NOT EXISTS idx_fields_status ON fields(status);
CREATE INDEX IF NOT EXISTS idx_fields_crop_type ON fields(crop_type);
CREATE INDEX IF NOT EXISTS idx_fields_owner_status ON fields(owner_id, status);

-- changeset aqua-ai:008-create-fields-unique-active-name-index dbms:postgresql
-- comment: Prevent duplicate field names for the same owner while active on PostgreSQL
CREATE UNIQUE INDEX IF NOT EXISTS idx_fields_owner_active_name ON fields(owner_id, LOWER(name)) WHERE status = 'ACTIVE';
