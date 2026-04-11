-- Let the database generate UUIDs instead of the application
ALTER TABLE records ALTER COLUMN uuid SET DEFAULT (UUID());
