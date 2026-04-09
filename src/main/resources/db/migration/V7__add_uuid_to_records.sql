-- Add uuid column to records
ALTER TABLE records ADD COLUMN uuid VARCHAR(36) NOT NULL DEFAULT '';

-- Backfill existing rows with generated UUIDs
UPDATE records SET uuid = UUID() WHERE uuid = '';

-- Add unique index
CREATE UNIQUE INDEX idx_records_uuid ON records (uuid);
