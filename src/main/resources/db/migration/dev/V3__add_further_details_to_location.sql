-- okres
ALTER TABLE location_tbl
    ADD COLUMN municipality varchar(50) NULL;

-- kraj
ALTER TABLE location_tbl
    ADD COLUMN county varchar(50) NULL;

-- region (e.g. Zapadni Cechy)
ALTER TABLE location_tbl
    ADD COLUMN region varchar(50) NULL;
