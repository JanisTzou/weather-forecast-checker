-- DROP DATABASE IF EXISTS weather_app_db;
-- CREATE DATABASE weather_app_db;

-- The first schema in the search path that exists is the default location for creating new objects. That is the reason that by default objects are created in the public schema.
-- When objects are referenced in any other context without schema qualification (table modification, data modification, or query commands) the search path is traversed until
-- a matching object is found. Therefore, in the default configuration, any unqualified access again can only refer to the public schema.


-- CREATE SCHEMA IF NOT EXISTS weather_app;
-- SET search_path TO weather_app;


REVOKE ALL PRIVILEGES ON DATABASE weather_app_db FROM janis;
-- DROP USER IF EXISTS janis;
-- CREATE USER janis WITH PASSWORD 'xxxxxx';
GRANT ALL PRIVILEGES ON DATABASE weather_app_db to janis;
GRANT ALL PRIVILEGES ON SCHEMA public to janis;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO janis;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO janis;

DROP TABLE IF EXISTS cloud_coverage_measurement_tbl;
DROP TABLE IF EXISTS hourly_forecast_tbl;
DROP TABLE IF EXISTS forecast_tbl;
DROP TABLE IF EXISTS location_tbl;
DROP TABLE IF EXISTS source_tbl;
DROP TABLE IF EXISTS provider_tbl;


CREATE TABLE IF NOT EXISTS source_tbl
(
    id   SERIAL PRIMARY KEY,
    name varchar(50) NULL,
    CONSTRAINT uc_source_tbl_name UNIQUE (name)
);

INSERT INTO source_tbl (name)
VALUES ('ACCUWATHER_API'),
       ('ALADIN_API'),
       ('CLEAR_OUTSIDE_WEB'),
       ('METEOBLUE_WEB'),
       ('METEOBLUE_API'),
       ('CHMU_WEB')
;


CREATE TABLE IF NOT EXISTS provider_tbl
(
    id   SERIAL PRIMARY KEY,
    name varchar(50) NULL,
    CONSTRAINT uc_provider_tbl_name UNIQUE (name)
);

INSERT INTO source_tbl (name)
VALUES ('ACCUWATHER'),
       ('ALADIN'),
       ('CLEAR_OUTSIDE'),
       ('METEOBLUE'),
       ('CHMU')
;


CREATE TABLE IF NOT EXISTS location_tbl
(
    id        SERIAL PRIMARY KEY,
    name      varchar(50) NOT NULL,
    latitude  int         NOT NULL,
    longitude int         NOT NULL,
    CONSTRAINT uc_location_tbl_name UNIQUE (name)
);


CREATE TABLE IF NOT EXISTS forecast_tbl
(
    id          SERIAL PRIMARY KEY,
    scraped     TIMESTAMP NOT NULL,
    source_id   int       NOT NULL,
    location_id int       NOT NULL,
    CONSTRAINT fk_forecast_tbl_source_tbl FOREIGN KEY (source_id) REFERENCES source_tbl (id),
    CONSTRAINT fk_forecast_tbl_location_tbl FOREIGN KEY (location_id) REFERENCES location_tbl (id)
);
CREATE INDEX IF NOT EXISTS fk_idx_forecast_tbl_source_tbl ON forecast_tbl (source_id);
CREATE INDEX IF NOT EXISTS fk_idx_forecast_tbl_location_tbl ON forecast_tbl (location_id);


CREATE TABLE IF NOT EXISTS hourly_forecast_tbl
(
    id             SERIAL PRIMARY KEY,
    forecast_id    int          NOT NULL,
    hour           TIMESTAMP    NOT NULL,
    cloud_coverage int          NULL,
    description    varchar(100) NULL,
    CONSTRAINT fk_hourly_forecast_tbl_forecast_tbl FOREIGN KEY (forecast_id) REFERENCES forecast_tbl (id)
);
CREATE INDEX IF NOT EXISTS fk_idx_hourly_forecast_tbl_forecast_tbl ON hourly_forecast_tbl (forecast_id);


CREATE TABLE IF NOT EXISTS cloud_coverage_measurement_tbl
(
    id             SERIAL PRIMARY KEY,
    scraped        TIMESTAMP    NOT NULL,
    date_time      TIMESTAMP    NOT NULL,
    source_id      int          NOT NULL,
    location_id    int          NOT NULL,
    cloud_coverage int          NULL,
    description    varchar(100) NULL,
    CONSTRAINT fk_cloud_coverage_measurement_tbl_source_tbl FOREIGN KEY (source_id) REFERENCES source_tbl (id),
    CONSTRAINT fk_cloud_coverage_measurement_tbl_location_tbl FOREIGN KEY (location_id) REFERENCES location_tbl (id)
);
CREATE INDEX IF NOT EXISTS fk_idx_cloud_coverage_measurement_tbl_source_tbl ON forecast_tbl (source_id);
CREATE INDEX IF NOT EXISTS fk_idx_cloud_coverage_measurement_tbl_location_tbl ON forecast_tbl (location_id);
