-- liquibase formatted sql

-- changeset aqua-ai:005-add-user-organization-and-preferences
-- comment: Add organization, telegram username, and preferred language to users table
ALTER TABLE users ADD COLUMN IF NOT EXISTS organization VARCHAR(128);
ALTER TABLE users ADD COLUMN IF NOT EXISTS telegram_username VARCHAR(64);
ALTER TABLE users ADD COLUMN IF NOT EXISTS preferred_language VARCHAR(8) DEFAULT 'uz';
