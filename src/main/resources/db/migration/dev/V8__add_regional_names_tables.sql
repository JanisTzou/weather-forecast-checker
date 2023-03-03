-- define new tables

CREATE TABLE IF NOT EXISTS municipality_tbl
(
    id   SERIAL PRIMARY KEY,
    name varchar(100) NOT NULL,
    CONSTRAINT uc_municipality_tbl_name UNIQUE (name)
);

CREATE TABLE IF NOT EXISTS county_tbl
(
    id   SERIAL PRIMARY KEY,
    name varchar(100) NOT NULL,
    CONSTRAINT uc_county_tbl_name UNIQUE (name)
);

CREATE TABLE IF NOT EXISTS region_tbl
(
    id   SERIAL PRIMARY KEY,
    name varchar(100) NOT NULL,
    CONSTRAINT uc_region_tbl_name UNIQUE (name)
);

-- populate new tables

INSERT INTO municipality_tbl (name)
SELECT DISTINCT municipality FROM location_tbl WHERE municipality IS NOT NULL ORDER BY municipality;

INSERT INTO county_tbl (name)
SELECT DISTINCT county FROM location_tbl WHERE county IS NOT NULL ORDER BY county;

INSERT INTO region_tbl (name)
SELECT DISTINCT region FROM location_tbl WHERE region IS NOT NULL ORDER BY region;


-- alter location table

ALTER TABLE location_tbl
    ADD COLUMN municipality_id int NULL,
    ADD COLUMN county_id       int NULL,
    ADD COLUMN region_id       int NULL,
    ADD CONSTRAINT fk_location_tbl_municipality_tbl FOREIGN KEY (municipality_id) REFERENCES municipality_tbl (id),
    ADD CONSTRAINT fk_location_tbl_county_tbl FOREIGN KEY (county_id) REFERENCES county_tbl (id),
    ADD CONSTRAINT fk_location_tbl_region_tbl FOREIGN KEY (region_id) REFERENCES region_tbl (id);

CREATE INDEX IF NOT EXISTS fk_idx_location_tbl_municipality_tbl ON location_tbl (municipality_id);
CREATE INDEX IF NOT EXISTS fk_idx_location_tbl_county_tbl ON location_tbl (county_id);
CREATE INDEX IF NOT EXISTS fk_idx_location_tbl_region_tbl ON location_tbl (region_id);

-- populate foreign keys in location tbl
UPDATE location_tbl
SET municipality_id = mt.id
FROM municipality_tbl mt
WHERE location_tbl.municipality = mt.name;

UPDATE location_tbl
SET county_id = ct.id
FROM county_tbl ct
WHERE location_tbl.county = ct.name;

UPDATE location_tbl
SET region_id = rt.id
FROM region_tbl rt
WHERE location_tbl.region = rt.name;


-- drop old columns

ALTER TABLE location_tbl
    DROP COLUMN municipality,
    DROP COLUMN county,
    DROP COLUMN region;
