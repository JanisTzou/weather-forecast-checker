SELECT substring(web.description, 6) AS icon, round(avg(web.cloud_coverage_total)) AS web_avg, round(avg(api.cloud_coverage_total)) api_avg, count(web.name) AS count
FROM (SELECT hour, st.name, cloud_coverage_total, description, location_id, date_trunc('hour', scraped) AS hour_scraped
      FROM hourly_forecast_tbl
               INNER JOIN forecast_tbl ft ON hourly_forecast_tbl.forecast_id = ft.id
               INNER JOIN source_tbl st ON st.id = ft.source_id
      WHERE ft.source_id = 4
     ) AS web
         INNER JOIN
     (SELECT hour, st.name, cloud_coverage_total, location_id, date_trunc('hour', scraped) AS hour_scraped
      FROM hourly_forecast_tbl
               INNER JOIN forecast_tbl ft ON hourly_forecast_tbl.forecast_id = ft.id
               INNER JOIN source_tbl st ON st.id = ft.source_id
      WHERE ft.source_id = 5
     ) AS api
     ON api.hour = web.hour
            AND api.location_id = web.location_id
            AND api.hour_scraped = web.hour_scraped
WHERE web.description LIKE 'icon_%'
GROUP BY description
ORDER BY icon
;

SELECT DISTINCT description
FROM hourly_forecast_tbl
WHERE description IS NOT NULL
ORDER BY description asc;
