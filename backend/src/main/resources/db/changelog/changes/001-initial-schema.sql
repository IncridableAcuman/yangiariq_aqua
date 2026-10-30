-- liquibase formatted sql

-- changeset aqua-ai:001-create-extensions
-- comment: Enable UUID extension if available
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- changeset aqua-ai:002-create-users-table
-- comment: Create foundational user table
CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    phone_number VARCHAR(32) NOT NULL UNIQUE,
    email VARCHAR(128) UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(128) NOT NULL,
    role VARCHAR(32) NOT NULL,
    district VARCHAR(64) DEFAULT 'Yangiariq',
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_users_phone ON users(phone_number);
CREATE INDEX IF NOT EXISTS idx_users_role ON users(role);

-- changeset aqua-ai:003-create-fields-table
-- comment: Create agricultural fields table
CREATE TABLE IF NOT EXISTS fields (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    farmer_id UUID NOT NULL,
    name VARCHAR(128) NOT NULL,
    area_hectares NUMERIC(10, 2) NOT NULL,
    crop_type VARCHAR(64) NOT NULL,
    soil_type VARCHAR(64),
    irrigation_method VARCHAR(32) NOT NULL,
    coordinates_geojson TEXT,
    canal_source VARCHAR(128),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT DEFAULT 0,
    CONSTRAINT fk_fields_farmer FOREIGN KEY (farmer_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_fields_farmer ON fields(farmer_id);
CREATE INDEX IF NOT EXISTS idx_fields_crop_type ON fields(crop_type);

-- changeset aqua-ai:004-create-irrigation-records-table
-- comment: Create water irrigation logs table
CREATE TABLE IF NOT EXISTS irrigation_records (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    field_id UUID NOT NULL,
    start_time TIMESTAMP WITH TIME ZONE NOT NULL,
    end_time TIMESTAMP WITH TIME ZONE,
    water_volume_m3 NUMERIC(12, 2) NOT NULL,
    water_source VARCHAR(128),
    recorded_by UUID NOT NULL,
    notes TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT DEFAULT 0,
    CONSTRAINT fk_irrigation_field FOREIGN KEY (field_id) REFERENCES fields(id) ON DELETE CASCADE,
    CONSTRAINT fk_irrigation_user FOREIGN KEY (recorded_by) REFERENCES users(id)
);

CREATE INDEX IF NOT EXISTS idx_irrigation_field ON irrigation_records(field_id);
CREATE INDEX IF NOT EXISTS idx_irrigation_start_time ON irrigation_records(start_time);

-- changeset aqua-ai:005-create-problem-reports-table
-- comment: Create problem reporting table
CREATE TABLE IF NOT EXISTS problem_reports (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    reported_by UUID NOT NULL,
    field_id UUID,
    title VARCHAR(160) NOT NULL,
    description TEXT NOT NULL,
    priority VARCHAR(32) NOT NULL DEFAULT 'MEDIUM',
    status VARCHAR(32) NOT NULL DEFAULT 'OPEN',
    location_details VARCHAR(255),
    resolved_at TIMESTAMP WITH TIME ZONE,
    resolution_notes TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT DEFAULT 0,
    CONSTRAINT fk_problem_user FOREIGN KEY (reported_by) REFERENCES users(id),
    CONSTRAINT fk_problem_field FOREIGN KEY (field_id) REFERENCES fields(id) ON DELETE SET NULL
);

CREATE INDEX IF NOT EXISTS idx_problem_status ON problem_reports(status);
CREATE INDEX IF NOT EXISTS idx_problem_priority ON problem_reports(priority);

-- changeset aqua-ai:006-create-notifications-table
-- comment: Create system and operational notifications table
CREATE TABLE IF NOT EXISTS notifications (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    recipient_id UUID NOT NULL,
    title VARCHAR(160) NOT NULL,
    message TEXT NOT NULL,
    type VARCHAR(32) NOT NULL,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT DEFAULT 0,
    CONSTRAINT fk_notifications_recipient FOREIGN KEY (recipient_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_notifications_recipient ON notifications(recipient_id, is_read);
