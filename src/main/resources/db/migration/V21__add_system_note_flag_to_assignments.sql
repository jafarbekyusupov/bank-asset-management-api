ALTER TABLE asset_assignments ADD COLUMN system_note BOOLEAN NOT NULL DEFAULT FALSE;

UPDATE asset_assignments
SET system_note = TRUE
WHERE return_notes LIKE 'Auto-%';
