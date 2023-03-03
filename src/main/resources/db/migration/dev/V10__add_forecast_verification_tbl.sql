CREATE TABLE IF NOT EXISTS forecast_verification_tbl
(
    id                       SERIAL PRIMARY KEY,
    created                  TIMESTAMP NOT NULL,
    type_id                  int       NOT NULL,
    source_id                int       NOT NULL,
    avg_forecast_cloud_total int       NOT NULL,
    avg_measured_cloud_total int       NOT NULL,
    avg_diff_abs             int       NOT NULL,
    avg_diff                 int       NOT NULL,
    record_count             int       NOT NULL,
    past_hours               int       NULL,
    day                      date      NULL,
    region_id                 int       null,
    county_id                 int       null,
    CONSTRAINT fk_forecast_verification_tbl_source_tbl FOREIGN KEY (source_id) REFERENCES source_tbl (id),
    CONSTRAINT fk_forecast_verification_tbl_forecast_verification_type_tbl FOREIGN KEY (type_id) REFERENCES forecast_verification_type_tbl (id),
    CONSTRAINT fk_forecast_verification_tbl_region_tbl FOREIGN KEY (region_id) REFERENCES region_tbl (id),
    CONSTRAINT fk_forecast_verification_tbl_county_tbl FOREIGN KEY (county_id) REFERENCES county_tbl (id)
);

CREATE INDEX IF NOT EXISTS fk_idx_forecast_verification_tbl_source_tbl ON forecast_verification_tbl (source_id);
CREATE INDEX IF NOT EXISTS fk_idx_forecast_verification_tbl_forecast_verification_type_tbl ON forecast_verification_tbl (type_id);
CREATE INDEX IF NOT EXISTS fk_idx_forecast_verification_tbl_region_tbl ON forecast_verification_tbl (region_id);
CREATE INDEX IF NOT EXISTS fk_idx_forecast_verification_tbl_county_tbl ON forecast_verification_tbl (county_id);
