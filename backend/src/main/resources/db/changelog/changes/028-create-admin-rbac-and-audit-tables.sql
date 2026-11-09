-- liquibase formatted sql

-- changeset aqua-ai:028-1-create-admin-rbac-and-audit-tables
-- comment: Milestone 08.20.1: Centralized RBAC, Roles, Permissions, and Security Audit Logging

CREATE TABLE IF NOT EXISTS roles (
    id UUID PRIMARY KEY,
    code VARCHAR(32) NOT NULL UNIQUE,
    name VARCHAR(64) NOT NULL,
    description TEXT,
    is_system BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS permissions (
    id UUID PRIMARY KEY,
    code VARCHAR(64) NOT NULL UNIQUE,
    name VARCHAR(128) NOT NULL,
    description TEXT,
    category VARCHAR(64) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS role_permissions (
    role_id UUID NOT NULL,
    permission_id UUID NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (role_id, permission_id),
    CONSTRAINT fk_rp_role FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE,
    CONSTRAINT fk_rp_permission FOREIGN KEY (permission_id) REFERENCES permissions(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS security_audit_logs (
    id UUID PRIMARY KEY,
    actor_id UUID,
    actor_name VARCHAR(128),
    actor_role VARCHAR(32),
    action VARCHAR(64) NOT NULL,
    target_type VARCHAR(64) NOT NULL,
    target_id VARCHAR(128),
    details_json TEXT,
    result VARCHAR(32) NOT NULL,
    ip_address VARCHAR(45),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_roles_code ON roles(code);
CREATE INDEX IF NOT EXISTS idx_permissions_code ON permissions(code);
CREATE INDEX IF NOT EXISTS idx_permissions_category ON permissions(category);
CREATE INDEX IF NOT EXISTS idx_security_audit_created ON security_audit_logs(created_at);
CREATE INDEX IF NOT EXISTS idx_security_audit_actor ON security_audit_logs(actor_id);
CREATE INDEX IF NOT EXISTS idx_security_audit_action ON security_audit_logs(action);
CREATE INDEX IF NOT EXISTS idx_security_audit_target ON security_audit_logs(target_type, target_id);

-- changeset aqua-ai:028-2-seed-roles-and-permissions
-- comment: Seed platform system roles and comprehensive permissions catalog

-- Seed Roles
INSERT INTO roles (id, code, name, description, is_system) VALUES
('00000000-0000-0000-0001-000000000001', 'SUPER_ADMIN', 'Super Administrator', 'Complete platform administration, global audit inspection, and governance control', true),
('00000000-0000-0000-0001-000000000002', 'ADMIN', 'Platform Administrator', 'User and operational management within regional water basin jurisdiction', true),
('00000000-0000-0000-0001-000000000003', 'OPERATOR', 'District Operator', 'Canal gate operations, telemetry collection, and water distribution triage', true),
('00000000-0000-0000-0001-000000000004', 'FARMER', 'Agricultural Producer', 'Parcel-level irrigation management, soil sensor view, and field observations', true);

-- Seed Permissions: USER_MANAGEMENT
INSERT INTO permissions (id, code, name, description, category) VALUES
('00000000-0000-0000-0002-000000000001', 'USER_VIEW', 'View Users', 'Allows viewing user directory and profiles', 'USER_MANAGEMENT'),
('00000000-0000-0000-0002-000000000002', 'USER_CREATE', 'Create Users', 'Allows provisioning new platform user accounts', 'USER_MANAGEMENT'),
('00000000-0000-0000-0002-000000000003', 'USER_UPDATE', 'Update Users', 'Allows editing profile, organization, and contact details', 'USER_MANAGEMENT'),
('00000000-0000-0000-0002-000000000004', 'USER_ACTIVATE', 'Activate Users', 'Allows activating suspended or pending accounts', 'USER_MANAGEMENT'),
('00000000-0000-0000-0002-000000000005', 'USER_DEACTIVATE', 'Deactivate Users', 'Allows deactivating or blocking user accounts', 'USER_MANAGEMENT'),
('00000000-0000-0000-0002-000000000006', 'USER_ROLE_ASSIGN', 'Assign Roles', 'Allows changing role assignments for users', 'USER_MANAGEMENT'),
('00000000-0000-0000-0002-000000000007', 'USER_DELETE', 'Delete Users', 'Allows soft-deleting and terminating user accounts', 'USER_MANAGEMENT');

-- Seed Permissions: ROLE_MANAGEMENT
INSERT INTO permissions (id, code, name, description, category) VALUES
('00000000-0000-0000-0002-000000000011', 'ROLE_VIEW', 'View Roles', 'Allows viewing platform roles and assigned permissions', 'ROLE_MANAGEMENT'),
('00000000-0000-0000-0002-000000000012', 'ROLE_CREATE', 'Create Roles', 'Allows defining new operational roles', 'ROLE_MANAGEMENT'),
('00000000-0000-0000-0002-000000000013', 'ROLE_UPDATE', 'Update Roles', 'Allows modifying role descriptions and permissions', 'ROLE_MANAGEMENT'),
('00000000-0000-0000-0002-000000000014', 'ROLE_DELETE', 'Delete Roles', 'Allows deleting custom non-system roles', 'ROLE_MANAGEMENT'),
('00000000-0000-0000-0002-000000000015', 'PERMISSION_VIEW', 'View Permissions', 'Allows inspecting the system permission catalog', 'ROLE_MANAGEMENT'),
('00000000-0000-0000-0002-000000000016', 'PERMISSION_ASSIGN', 'Assign Permissions', 'Allows granting or revoking permissions from roles', 'ROLE_MANAGEMENT');

-- Seed Permissions: FARM_FIELD_MANAGEMENT
INSERT INTO permissions (id, code, name, description, category) VALUES
('00000000-0000-0000-0002-000000000021', 'FARM_VIEW', 'View Farms', 'Allows viewing registered farms and boundaries', 'FARM_FIELD_MANAGEMENT'),
('00000000-0000-0000-0002-000000000022', 'FARM_MANAGE', 'Manage Farms', 'Allows registering and updating farm boundaries', 'FARM_FIELD_MANAGEMENT'),
('00000000-0000-0000-0002-000000000023', 'FIELD_VIEW', 'View Fields', 'Allows viewing field parcels and soil metadata', 'FARM_FIELD_MANAGEMENT'),
('00000000-0000-0000-0002-000000000024', 'FIELD_MANAGE', 'Manage Fields', 'Allows creating, updating, and surveying parcels', 'FARM_FIELD_MANAGEMENT');

-- Seed Permissions: SENSOR_MANAGEMENT
INSERT INTO permissions (id, code, name, description, category) VALUES
('00000000-0000-0000-0002-000000000031', 'SENSOR_VIEW', 'View Sensors', 'Allows viewing IoT telemetry and sensor health', 'SENSOR_MANAGEMENT'),
('00000000-0000-0000-0002-000000000032', 'SENSOR_MANAGE', 'Manage Sensors', 'Allows provisioning, calibrating, and updating sensors', 'SENSOR_MANAGEMENT');

-- Seed Permissions: WATER_MANAGEMENT
INSERT INTO permissions (id, code, name, description, category) VALUES
('00000000-0000-0000-0002-000000000041', 'WATER_ALLOCATION_VIEW', 'View Water Allocations', 'Allows viewing canal quotas and turnouts', 'WATER_MANAGEMENT'),
('00000000-0000-0000-0002-000000000042', 'WATER_ALLOCATION_MANAGE', 'Manage Water Allocations', 'Allows configuring regional water dispatch', 'WATER_MANAGEMENT'),
('00000000-0000-0000-0002-000000000043', 'IRRIGATION_VIEW', 'View Irrigation Plans', 'Allows inspecting smart irrigation schedules', 'WATER_MANAGEMENT'),
('00000000-0000-0000-0002-000000000044', 'IRRIGATION_MANAGE', 'Manage Irrigation Plans', 'Allows generating and approving irrigation plans', 'WATER_MANAGEMENT');

-- Seed Permissions: REPORTING
INSERT INTO permissions (id, code, name, description, category) VALUES
('00000000-0000-0000-0002-000000000051', 'REPORT_VIEW', 'View Reports', 'Allows accessing regional and farm performance reports', 'REPORTING'),
('00000000-0000-0000-0002-000000000052', 'REPORT_EXPORT', 'Export Reports', 'Allows exporting water accounting and audit reports', 'REPORTING');

-- Seed Permissions: GOVERNANCE
INSERT INTO permissions (id, code, name, description, category) VALUES
('00000000-0000-0000-0002-000000000061', 'AUDIT_LOG_VIEW', 'View Audit Logs', 'Allows inspecting administrative security audit logs', 'GOVERNANCE'),
('00000000-0000-0000-0002-000000000062', 'SYSTEM_SETTINGS_VIEW', 'View System Settings', 'Allows inspecting runtime environment configuration', 'GOVERNANCE'),
('00000000-0000-0000-0002-000000000063', 'SYSTEM_SETTINGS_MANAGE', 'Manage System Settings', 'Allows modifying system configuration flags', 'GOVERNANCE');

-- Seed Permissions: PILOT_MANAGEMENT
INSERT INTO permissions (id, code, name, description, category) VALUES
('00000000-0000-0000-0002-000000000071', 'PILOT_VIEW', 'View Pilot Programs', 'Allows viewing field pilots and ground-truth validation records', 'PILOT_MANAGEMENT'),
('00000000-0000-0000-0002-000000000072', 'PILOT_MANAGE', 'Manage Pilot Programs', 'Allows creating, approving, and activating field pilots', 'PILOT_MANAGEMENT');

-- Seed Permissions: ACCOUNTABILITY
INSERT INTO permissions (id, code, name, description, category) VALUES
('00000000-0000-0000-0002-000000000081', 'INCIDENT_VIEW', 'View Incidents', 'Allows viewing water dispute, theft, and equipment incidents', 'ACCOUNTABILITY'),
('00000000-0000-0000-0002-000000000082', 'INCIDENT_MANAGE', 'Manage Incidents', 'Allows investigating incidents and tracking corrective actions', 'ACCOUNTABILITY');

-- changeset aqua-ai:028-3-assign-role-permissions
-- comment: Map permissions to SUPER_ADMIN, ADMIN, OPERATOR, and FARMER

-- SUPER_ADMIN has ALL permissions
INSERT INTO role_permissions (role_id, permission_id)
SELECT '00000000-0000-0000-0001-000000000001', id FROM permissions;

-- ADMIN permissions: All operational & governance permissions except custom role deletion
INSERT INTO role_permissions (role_id, permission_id)
SELECT '00000000-0000-0000-0001-000000000002', id FROM permissions
WHERE code NOT IN ('ROLE_DELETE', 'SYSTEM_SETTINGS_MANAGE');

-- OPERATOR permissions: Operational field monitoring, incident management, reporting
INSERT INTO role_permissions (role_id, permission_id)
SELECT '00000000-0000-0000-0001-000000000003', id FROM permissions
WHERE code IN (
    'USER_VIEW',
    'FARM_VIEW', 'FIELD_VIEW',
    'SENSOR_VIEW', 'SENSOR_MANAGE',
    'WATER_ALLOCATION_VIEW', 'WATER_ALLOCATION_MANAGE',
    'IRRIGATION_VIEW', 'IRRIGATION_MANAGE',
    'REPORT_VIEW', 'REPORT_EXPORT',
    'PILOT_VIEW', 'PILOT_MANAGE',
    'INCIDENT_VIEW', 'INCIDENT_MANAGE'
);

-- FARMER permissions: Farm parcel viewing, field management, own sensor telemetry, irrigation viewing, pilot view
INSERT INTO role_permissions (role_id, permission_id)
SELECT '00000000-0000-0000-0001-000000000004', id FROM permissions
WHERE code IN (
    'FARM_VIEW', 'FIELD_VIEW', 'FIELD_MANAGE',
    'SENSOR_VIEW',
    'IRRIGATION_VIEW', 'IRRIGATION_MANAGE',
    'REPORT_VIEW',
    'PILOT_VIEW',
    'INCIDENT_VIEW'
);
