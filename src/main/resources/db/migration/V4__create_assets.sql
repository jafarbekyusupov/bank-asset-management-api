CREATE TYPE asset_status AS ENUM (
    'REGISTERED',
    'ASSIGNED',
    'IN_REPAIR',
    'LOST',
    'WRITTEN_OFF'
);

CREATE TABLE assets (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name                VARCHAR(200) NOT NULL,
    description         TEXT,
    serial_number       VARCHAR(100) NOT NULL UNIQUE,
    brand               VARCHAR(100),
    model               VARCHAR(100),
    category_id         UUID NOT NULL REFERENCES asset_categories(id) ON DELETE RESTRICT,
    type_id             UUID NOT NULL REFERENCES asset_types(id) ON DELETE RESTRICT,

    status              asset_status NOT NULL DEFAULT 'REGISTERED',

    owner_id    UUID REFERENCES users(id) ON DELETE SET NULL,
    dept_id     UUID REFERENCES departments(id) ON DELETE SET NULL,

    purchase_date       DATE,
    warranty_until      DATE,
    purchase_price      DECIMAL(12, 2),

    -- flexible specs based on asset type
    -- Laptop:  {"ram": "16GB", "cpu": "i7-12th", "storage": "512GB", "os": "Windows 11"}
    -- Monitor: {"size": "27inch", "resolution": "4K", "panel": "IPS"}
    -- Printer: {"color": false, "ppm": 30, "type": "laser"}
    specifications      JSONB DEFAULT '{}',

    image_url           VARCHAR(500),
    notes               TEXT,
    created_by          UUID REFERENCES users(id) ON DELETE SET NULL,
    created_at          TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_assets_status      ON assets(status);
CREATE INDEX idx_assets_category    ON assets(category_id);
CREATE INDEX idx_assets_type        ON assets(type_id);
CREATE INDEX idx_assets_owner       ON assets(owner_id);
CREATE INDEX idx_assets_dept        ON assets(dept_id);
CREATE INDEX idx_assets_serial      ON assets(serial_number);
CREATE INDEX idx_assets_warranty    ON assets(warranty_until);
CREATE INDEX idx_assets_specs       ON assets USING gin(specifications);
