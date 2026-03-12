CREATE TYPE user_role AS ENUM ('ADMIN', 'STAFF');
CREATE TYPE user_status AS ENUM ('PENDING', 'ACTIVE', 'SUSPENDED');

CREATE TABLE users (
    id                    UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    full_name             VARCHAR(150) NOT NULL,
    email                 VARCHAR(150) NOT NULL UNIQUE,
    password_hash         VARCHAR(255) NOT NULL,
    role                  user_role NOT NULL DEFAULT 'STAFF',
    status                user_status NOT NULL DEFAULT 'PENDING',

    department_id         UUID REFERENCES departments(id) ON DELETE SET NULL,
    branch_id             UUID REFERENCES branches(id) ON DELETE SET NULL,

    verification_token    VARCHAR(255),
    token_expires_at      TIMESTAMP,

    created_at            TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at            TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_status ON users(status);

INSERT INTO users (full_name, email, password_hash, role, status)
VALUES (
    'demo-admin',
    'dadmin@bank.com',
    '$2a$12$h38cUZl9/d13fO0mhZIGDuklmtllT5EG/Z.dD3qd8qt3hbf6d7uXO',
    'ADMIN',
    'ACTIVE'
);
