-- liquibase formatted sql

-- changeset aqua-ai:014-add-water-sources-location
-- comment: Add optional latitude and longitude coordinates for water source infrastructure mapping
ALTER TABLE water_sources ADD COLUMN latitude NUMERIC(9, 6);
ALTER TABLE water_sources ADD COLUMN longitude NUMERIC(9, 6);

ALTER TABLE water_sources ADD CONSTRAINT chk_water_source_latitude CHECK (latitude IS NULL OR (latitude >= -90.0 AND latitude <= 90.0));
ALTER TABLE water_sources ADD CONSTRAINT chk_water_source_longitude CHECK (longitude IS NULL OR (longitude >= -180.0 AND longitude <= 180.0));
