-- liquibase formatted sql

-- changeset aqua-ai:029-assign-designated-admins
-- comment: Ensure abdusharipovizzat03@gmail.com and izzatbek.engineer@gmail.com are marked as ADMIN
UPDATE users
SET role = 'ADMIN',
    updated_at = CURRENT_TIMESTAMP
WHERE LOWER(email) IN ('abdusharipovizzat03@gmail.com', 'izzatbek.engineer@gmail.com')
  AND role NOT IN ('ADMIN', 'SUPER_ADMIN');
