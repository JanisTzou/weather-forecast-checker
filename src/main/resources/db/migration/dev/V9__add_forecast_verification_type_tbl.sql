CREATE TABLE IF NOT EXISTS forecast_verification_type_tbl
(
    id   SERIAL PRIMARY KEY,
    name varchar(50) NULL,
    CONSTRAINT uc_forecast_verification_type_tbl_name UNIQUE (name)
);

INSERT INTO forecast_verification_type_tbl (name)
VALUES ('PAST_N_HOURS'),
       ('DAILY'),
       ('ALL_TIME')
;
