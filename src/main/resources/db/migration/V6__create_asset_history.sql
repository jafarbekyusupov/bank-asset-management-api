CREATE TYPE asset_action AS ENUM (
    'CREATED',          -- asset first registered
    'ASSIGNED',         -- given to user or dept
    'RETURNED',         -- taken back to stock
    'STATUS_CHANGED',   -- e.g ASSIGNED -> IN_REPAIR
    'UPDATED',          -- metadata edited (brand, model, specs, etc.)
    'IMAGE_UPLOADED',   -- img attached
    'NOTE_ADDED'        -- manual note added
);

CREATE TABLE asset_history (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    asset_id        UUID NOT NULL REFERENCES assets(id) ON DELETE CASCADE,

    action          asset_action NOT NULL,

    old_status      asset_status,
    new_status      asset_status,

    from_user_id    UUID REFERENCES users(id) ON DELETE SET NULL,
    to_user_id      UUID REFERENCES users(id) ON DELETE SET NULL,
    changed_by      UUID NOT NULL REFERENCES users(id) ON DELETE RESTRICT,

    changed_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    reason          TEXT,
    metadata        JSONB DEFAULT '{}'
);

CREATE INDEX idx_history_asset      ON asset_history(asset_id);
CREATE INDEX idx_history_changed_by ON asset_history(changed_by);
CREATE INDEX idx_history_action     ON asset_history(action);
CREATE INDEX idx_history_changed_at ON asset_history(changed_at DESC);
