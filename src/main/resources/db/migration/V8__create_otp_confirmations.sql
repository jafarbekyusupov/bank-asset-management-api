CREATE TYPE otp_purpose AS ENUM ('REGISTRATION', 'PASSWORD_RESET');

CREATE TABLE otp_confirmations (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    code        VARCHAR(6) NOT NULL,
    purpose     otp_purpose NOT NULL,
    expires_at  TIMESTAMP NOT NULL,
    used_at     TIMESTAMP,
    created_at  TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_otp_user_purpose ON otp_confirmations(user_id, purpose);
CREATE INDEX idx_otp_expires      ON otp_confirmations(expires_at);
