ALTER TABLE asset_history ADD COLUMN system_note BOOLEAN NOT NULL DEFAULT FALSE;

UPDATE asset_history
SET system_note = TRUE
WHERE reason LIKE 'Auto-%';
