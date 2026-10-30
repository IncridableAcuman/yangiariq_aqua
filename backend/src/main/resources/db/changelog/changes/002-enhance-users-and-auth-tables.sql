-- liquibase formatted sql

-- changeset aqua-ai:002-add-user-name-and-status-columns
-- comment: Enhance users table with first_name, last_name, and explicit status enum
ALTER TABLE users ADD COLUMN IF NOT EXISTS first_name VARCHAR(64) DEFAULT 'Farmer' NOT NULL;
ALTER TABLE users ADD COLUMN IF NOT EXISTS last_name VARCHAR(64) DEFAULT 'User' NOT NULL;
ALTER TABLE users ADD COLUMN IF NOT EXISTS status VARCHAR(32) DEFAULT 'ACTIVE' NOT NULL;

-- changeset aqua-ai:003-create-refresh-token-sessions-table
-- comment: Create persistent refresh token session tracker for multi-device support and token rotation
CREATE TABLE IF NOT EXISTS refresh_token_sessions (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    user_id UUID NOT NULL,
    token_hash VARCHAR(64) NOT NULL,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    revoked_at TIMESTAMP WITH TIME ZONE,
    replaced_by_session_id UUID,
    device_info VARCHAR(255),
    ip_address VARCHAR(45),
    CONSTRAINT fk_refresh_token_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_refresh_token_user_id ON refresh_token_sessions(user_id);
CREATE INDEX IF NOT EXISTS idx_refresh_token_hash ON refresh_token_sessions(token_hash);

-- changeset aqua-ai:004-create-password-reset-tokens-table
-- comment: Create secure hashed single-use password reset tokens
CREATE TABLE IF NOT EXISTS password_reset_tokens (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    user_id UUID NOT NULL,
    token_hash VARCHAR(64) NOT NULL UNIQUE,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    used_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_pwd_reset_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_pwd_reset_token_hash ON password_reset_tokens(token_hash);
