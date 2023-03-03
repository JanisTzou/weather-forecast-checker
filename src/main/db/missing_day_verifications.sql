SELECT DISTINCT DATE(ft.scraped - INTERVAL '1 day') AS date
FROM forecast_tbl ft
WHERE DATE(ft.scraped - INTERVAL '1 day') NOT IN
      (SELECT DISTINCT day
       FROM forecast_verification_tbl
       WHERE day IS NOT NULL)
ORDER BY date;


SELECT DISTINCT DATE(ft.scraped - INTERVAL '1 day') AS date
FROM forecast_tbl ft;

