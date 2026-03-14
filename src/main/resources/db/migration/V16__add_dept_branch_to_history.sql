ALTER TABLE asset_history
    ADD COLUMN from_dept_id   UUID REFERENCES departments(id),
    ADD COLUMN to_dept_id     UUID REFERENCES departments(id),
    ADD COLUMN from_branch_id UUID REFERENCES branches(id),
    ADD COLUMN to_branch_id   UUID REFERENCES branches(id);
