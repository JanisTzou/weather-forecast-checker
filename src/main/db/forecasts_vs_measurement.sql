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
       round(avg(measured_total)) AS avg_measured_total,
       round(avg(forecast_total)) AS avg_forecast_total,
       round(avg(diff_abs))       AS avg_diff_abs,
       round(avg(diff))           AS avg_diff,
       count(hour)                AS record_count
FROM (SELECT frcst.source_name                                                                   AS source,
             msrmt.name                                                                          AS location,
             frcst.forecast_scraped_dt                                                           AS forecast_scraped_dt,
             frcst.forecast_scraped_hour_dt                                                      AS forecast_scraped_hour_dt,
             frcst.hour,
             msrmt.date_time,
             msrmt.cloud_coverage_total                                                          AS measured_total,
             frcst.cloud_coverage_total                                                          AS forecast_total,
             abs(msrmt.cloud_coverage_total - frcst.cloud_coverage_total)                        AS diff_abs,
             msrmt.cloud_coverage_total - frcst.cloud_coverage_total                             AS diff,
             extract(epoch FROM (msrmt.scraped_hour_dt - frcst.forecast_scraped_hour_dt)) / 3600 AS hours_after_forecast
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
--             WHERE hft.hour = (date_trunc('hour', ft.scraped) + interval '6 hours')
               WHERE hft.hour >= ft.scraped -- ensures that we do not include "forecast of the past"
--               AND ft.location_id = 19
--               AND ft.source_id = 4
              AND (CASE
                       WHEN (true) THEN hft.hour >= NOW() - interval '200 hours' AND hft.hour <= NOW()
                       WHEN (false) THEN hft.hour >= '2023-03-01 00:00:00' AND hft.hour <= '2023-03-01 23:59:59'
                       ELSE true
                END)
            GROUP BY ft.source_id, st.name, ft.location_id, hft.hour
           ) AS frcst
               INNER JOIN (SELECT max(ccmt.id),
                                  ccmt.location_id,
                                  lt.name,
                                  ccmt.date_time,
                                  avg(ccmt.cloud_coverage_total)        AS cloud_coverage_total,
                                  date_trunc('hour', max(ccmt.scraped)) AS scraped_hour_dt
                           FROM cloud_coverage_measurement_tbl AS ccmt
                                    INNER JOIN location_tbl AS lt ON ccmt.location_id = lt.id
                                    INNER JOIN county_tbl AS c ON lt.county_id = c.id
                                        AND (CASE WHEN (false) THEN c.name in ('Středočeský kraj') ELSE true END)
                           WHERE source_id = 6
                             AND ccmt.cloud_coverage_total IS NOT NULL
                           GROUP BY ccmt.location_id, lt.name, ccmt.date_time
      ) AS msrmt ON frcst.location_id = msrmt.location_id
          AND frcst.hour = msrmt.date_time
     ) verification
GROUP BY source
ORDER BY avg_diff_abs
;
