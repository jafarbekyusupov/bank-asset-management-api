CREATE TABLE audit_logs (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         UUID REFERENCES users(id) ON DELETE SET NULL,
    user_email      VARCHAR(150),

    http_method     VARCHAR(10) NOT NULL,
    endpoint        VARCHAR(500) NOT NULL,

    entity_type     VARCHAR(50),
    entity_id       VARCHAR(100),

    ip_address      VARCHAR(50),
    user_agent      TEXT,

    response_status INTEGER,
    duration_ms     INTEGER,

    request_payload JSONB,
    timestamp       TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_audit_user         ON audit_logs(user_id);
CREATE INDEX idx_audit_timestamp    ON audit_logs(timestamp DESC);
CREATE INDEX idx_audit_entity       ON audit_logs(entity_type, entity_id);
CREATE INDEX idx_audit_endpoint     ON audit_logs(endpoint);
