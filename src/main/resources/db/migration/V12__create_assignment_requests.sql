CREATE TYPE assignment_request_status AS ENUM ('PENDING', 'APPROVED', 'REJECTED');

CREATE TABLE assignment_requests (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    asset_id        UUID NOT NULL REFERENCES assets(id) ON DELETE CASCADE,
    requested_by    UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    reason          TEXT,
    status          assignment_request_status NOT NULL DEFAULT 'PENDING',
    reviewed_by     UUID REFERENCES users(id) ON DELETE SET NULL,
    admin_note      TEXT,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    reviewed_at     TIMESTAMP
);

CREATE INDEX idx_assignment_requests_asset  ON assignment_requests(asset_id);
CREATE INDEX idx_assignment_requests_user   ON assignment_requests(requested_by);
CREATE INDEX idx_assignment_requests_status ON assignment_requests(status);
