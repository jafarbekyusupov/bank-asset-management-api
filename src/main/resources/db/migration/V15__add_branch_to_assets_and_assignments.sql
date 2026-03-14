ALTER TABLE assets ADD COLUMN branch_id UUID REFERENCES branches(id);
ALTER TABLE asset_assignments ADD COLUMN assigned_to_branch_id UUID REFERENCES branches(id);
