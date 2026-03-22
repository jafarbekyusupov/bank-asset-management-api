CREATE EXTENSION IF NOT EXISTS pg_trgm;

CREATE INDEX IF NOT EXISTS idx_assets_name_trgm ON assets USING GIN (lower(name) gin_trgm_ops);
CREATE INDEX IF NOT EXISTS idx_assets_brand_trgm ON assets USING GIN (lower(brand) gin_trgm_ops);
CREATE INDEX IF NOT EXISTS idx_assets_model_trgm ON assets USING GIN (lower(model) gin_trgm_ops);
CREATE INDEX IF NOT EXISTS idx_assets_description_trgm ON assets USING GIN (lower(description) gin_trgm_ops);
