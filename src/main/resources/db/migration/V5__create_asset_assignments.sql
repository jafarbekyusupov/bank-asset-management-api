CREATE TABLE asset_assignments (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    asset_id                UUID NOT NULL REFERENCES assets(id) ON DELETE CASCADE,

    assigned_to_user_id     UUID REFERENCES users(id) ON DELETE SET NULL,
    assigned_to_dept_id     UUID REFERENCES departments(id) ON DELETE SET NULL,

    assigned_by             UUID NOT NULL REFERENCES users(id) ON DELETE RESTRICT,

    assigned_at             TIMESTAMP NOT NULL DEFAULT NOW(),
    returned_at             TIMESTAMP,
    return_notes            TEXT,
    notes                   TEXT
);

CREATE UNIQUE INDEX idx_one_active_assignment
    ON asset_assignments(asset_id)
    WHERE returned_at IS NULL;

CREATE INDEX idx_assignments_asset   ON asset_assignments(asset_id);
CREATE INDEX idx_assignments_user    ON asset_assignments(assigned_to_user_id);
CREATE INDEX idx_assignments_dept    ON asset_assignments(assigned_to_dept_id);
CREATE INDEX idx_assignments_active  ON asset_assignments(asset_id, returned_at);
