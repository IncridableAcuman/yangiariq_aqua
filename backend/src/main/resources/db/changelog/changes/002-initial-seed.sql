-- liquibase formatted sql

-- changeset aqua-ai:007-seed-initial-admin
-- comment: Insert default district admin and operator for Yangiariq water basin
-- Passwords below are bcrypt hash for: 'admin123'
INSERT INTO users (id, phone_number, email, password_hash, full_name, role, district, active, created_at, updated_at, version)
VALUES (
    '00000000-0000-0000-0000-000000000001',
    '+998901234567',
    'admin@yangiariq.aquaai.uz',
    '$2a$10$7EqJtq98hPqEX7fNZaFWoO.8/b822dI9u64P314k5zRkH6m.kC2Wy',
    'Yangiariq District Super Admin',
    'ADMIN',
    'Yangiariq',
    TRUE,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    0
) ON CONFLICT (phone_number) DO NOTHING;

INSERT INTO users (id, phone_number, email, password_hash, full_name, role, district, active, created_at, updated_at, version)
VALUES (
    '00000000-0000-0000-0000-000000000002',
    '+998907654321',
    'operator@yangiariq.aquaai.uz',
    '$2a$10$7EqJtq98hPqEX7fNZaFWoO.8/b822dI9u64P314k5zRkH6m.kC2Wy',
    'Canal Dispatch Operator',
    'OPERATOR',
    'Yangiariq',
    TRUE,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    0
) ON CONFLICT (phone_number) DO NOTHING;
