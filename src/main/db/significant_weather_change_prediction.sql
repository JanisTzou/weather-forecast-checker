SELECT hour, st.name, cloud_coverage_total, location_id, date_trunc('hour', scraped) AS forecast_scraped
FROM hourly_forecast_tbl
         INNER JOIN forecast_tbl ft ON hourly_forecast_tbl.forecast_id = ft.id
         INNER JOIN source_tbl st ON st.id = ft.source_id
WHERE ft.source_id = 4
;

SELECT hour, source_id, date_trunc('hour', scraped) AS scraped
FROM hourly_forecast_tbl
         INNER JOIN forecast_tbl ft ON hourly_forecast_tbl.forecast_id = ft.id
WHERE source_id = 1
;

SELECT source,
       round(avg(measured_1)) AS avg_measured_total,
       round(avg(forecast_1)) AS avg_forecast_total,
       count(hour_1)          AS record_count
FROM (SELECT 10                                                          AS hour_diff,
             frcst_1.source_name                                         AS source,
             msrmt_1.name                                                AS location,
             frcst_1.forecast_scraped_dt                                 AS forecast_scraped_dt,
             frcst_1.forecast_scraped_hour_dt                            AS forecast_scraped_hour_dt,
             frcst_1.hour                                                AS hour_1,
             frcst_2.hour                                                AS hour_2,
             msrmt_1.cloud_coverage_total                                AS measured_1,
             msrmt_2.cloud_coverage_total                                AS measured_2,
             frcst_1.cloud_coverage_total                                AS forecast_1,
             frcst_2.cloud_coverage_total                                AS forecast_2,
             msrmt_2.cloud_coverage_total - msrmt_1.cloud_coverage_total AS measured_change,
             frcst_2.cloud_coverage_total - frcst_1.cloud_coverage_total AS forecast_change
      FROM (SELECT ft.source_id,
                   st.name                             AS source_name,
                   ft.location_id,
                   hft.hour,
                   max(hft.id)                         AS hour_id,
                   max(ft.id)                          AS forecast_id,
                   date_trunc('hour', max(ft.scraped)) AS forecast_scraped_hour_dt,
                   max(ft.scraped)                     AS forecast_scraped_dt,
                   max(hft.cloud_coverage_total)       AS cloud_coverage_total
            FROM hourly_forecast_tbl hft
                     INNER JOIN forecast_tbl ft ON hft.forecast_id = ft.id
                     INNER JOIN source_tbl st ON st.id = ft.source_id
            WHERE hft.hour >= ft.scraped -- ensures that we do not include "forecast of the past"
--               AND ft.location_id = 1
--               AND ft.source_id = 2
            GROUP BY ft.source_id, st.name, ft.location_id, hft.hour
           ) AS frcst_1
           INNER JOIN (SELECT ft.source_id,
                               ft.location_id,
                               hft.hour,
                               date_trunc('hour', max(ft.scraped)) AS forecast_scraped_hour_dt,
                               max(ft.scraped)                     AS forecast_scraped_dt,
                               max(hft.cloud_coverage_total)       AS cloud_coverage_total
                        FROM hourly_forecast_tbl hft
                                 INNER JOIN forecast_tbl ft ON hft.forecast_id = ft.id
                                 INNER JOIN source_tbl st ON st.id = ft.source_id
                        WHERE hft.hour >= ft.scraped -- ensures that we do not include "forecast of the past"
                        GROUP BY ft.source_id, st.name, ft.location_id, hft.hour
                        ) AS frcst_2 ON frcst_1.location_id = frcst_2.location_id
                                            AND frcst_1.source_id = frcst_2.source_id
                                            AND frcst_1.hour = (frcst_2.hour - interval '12 hours')
           INNER JOIN (SELECT max(ccmt.id),
                              ccmt.location_id,
                              lt.name,
                              ccmt.date_time,
                              avg(ccmt.cloud_coverage_total)        AS cloud_coverage_total,
                              date_trunc('hour', max(ccmt.scraped)) AS scraped_hour_dt
                       FROM cloud_coverage_measurement_tbl AS ccmt
                                INNER JOIN location_tbl AS lt ON ccmt.location_id = lt.id
                                INNER JOIN region_tbl AS r ON lt.region_id = r.id
                           AND (CASE WHEN (false) THEN r.name in ('Střední Čechy') ELSE true END)
                                INNER JOIN county_tbl AS c ON lt.county_id = c.id
                           AND (CASE WHEN (false) THEN c.name in ('Středočeský kraj') ELSE true END)
                       WHERE source_id = 6
                         AND ccmt.cloud_coverage_total IS NOT NULL
                       GROUP BY ccmt.location_id, lt.name, ccmt.date_time
            ) AS msrmt_1 ON frcst_1.location_id = msrmt_1.location_id
                        AND frcst_1.hour = msrmt_1.date_time
           INNER JOIN (SELECT max(ccmt.id),
                              ccmt.location_id,
                              ccmt.date_time,
                              avg(ccmt.cloud_coverage_total)        AS cloud_coverage_total,
                              date_trunc('hour', max(ccmt.scraped)) AS scraped_hour_dt
                       FROM cloud_coverage_measurement_tbl AS ccmt
                       WHERE source_id = 6
                         AND ccmt.cloud_coverage_total IS NOT NULL
                       GROUP BY ccmt.location_id, ccmt.location_id, ccmt.date_time
          ) AS msrmt_2 ON frcst_2.location_id = msrmt_2.location_id
              AND frcst_2.hour = msrmt_2.date_time
     ) verification
GROUP BY source
;
