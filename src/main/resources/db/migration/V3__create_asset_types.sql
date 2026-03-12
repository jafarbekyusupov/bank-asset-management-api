CREATE TABLE asset_categories (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE asset_types (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) NOT NULL,
    category_id UUID NOT NULL REFERENCES asset_categories(id) ON DELETE RESTRICT,
    description VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE(name, category_id)
);

INSERT INTO asset_categories (name, description) VALUES
    ('IT', 'Computers, networking, and technology equipment'),
    ('Office', 'Furniture and general office equipment'),
    ('Security', 'Access control and surveillance equipment'),
    ('Facilities', 'Building and infrastructure equipment'),
    ('Other', 'Miscellaneous assets');

INSERT INTO asset_types (name, category_id)
SELECT name, (SELECT id FROM asset_categories WHERE name = 'IT')
FROM (VALUES
    ('Laptop'),
    ('Desktop'),
    ('Monitor'),
    ('Printer'),
    ('Scanner'),
    ('Network Switch'),
    ('Router'),
    ('UPS'),
    ('Server'),
    ('Keyboard'),
    ('Mouse'),
    ('Headset'),
    ('Webcam'),
    ('Docking Station'),
    ('External Drive')
) AS t(name);

INSERT INTO asset_types (name, category_id)
SELECT name, (SELECT id FROM asset_categories WHERE name = 'Office')
FROM (VALUES
    ('Desk'),
    ('Chair'),
    ('Cabinet'),
    ('Whiteboard'),
    ('Projector'),
    ('Phone')
) AS t(name);

INSERT INTO asset_types (name, category_id)
SELECT name, (SELECT id FROM asset_categories WHERE name = 'Security')
FROM (VALUES
    ('Badge Reader'),
    ('IP Camera'),
    ('Safe'),
    ('Alarm Panel')
) AS t(name);

INSERT INTO asset_types (name, category_id)
SELECT name, (SELECT id FROM asset_categories WHERE name = 'Facilities')
FROM (VALUES
    ('Air Conditioner'),
    ('Generator'),
    ('Fire Extinguisher')
) AS t(name);
