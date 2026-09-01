-- Make client_id optional in projects table to support projects without an assigned client
ALTER TABLE projects ALTER COLUMN client_id DROP NOT NULL;
