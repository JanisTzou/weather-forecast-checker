DROP INDEX IF EXISTS fk_idx_forecast_verification_tbl_region_tbl;

ALTER TABLE forecast_verification_tbl
    DROP CONSTRAINT fk_forecast_verification_tbl_region_tbl,
    DROP COLUMN region_id
;
