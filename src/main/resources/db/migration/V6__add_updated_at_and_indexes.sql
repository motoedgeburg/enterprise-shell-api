-- Add updated_at column to records
ALTER TABLE records ADD COLUMN updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;

-- Backfill updated_at with created_at for existing rows
UPDATE records SET updated_at = created_at WHERE updated_at IS NULL;

-- Add indexes on commonly filtered columns
CREATE INDEX idx_records_department ON records (department);
CREATE INDEX idx_records_status ON records (status);
CREATE INDEX idx_records_name ON records (name);
