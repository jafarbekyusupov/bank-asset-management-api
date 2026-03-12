CREATE TABLE branches (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name  VARCHAR(100) NOT NULL,
    location VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE departments (
    id  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) NOT NULL,
    branch_id UUID REFERENCES branches(id) ON DELETE SET NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- seed data
INSERT INTO branches (name, location) VALUES
    ('Main Branch', 'Headquarters'),
    ('Yunusabad Branch', 'Yunusabad District'),
    ('Mirzo-Ulugbek Branch', 'Mirzo-Ulugbek District');
