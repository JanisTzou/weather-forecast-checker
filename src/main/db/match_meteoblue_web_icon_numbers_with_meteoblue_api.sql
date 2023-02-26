select meteo_web.hour, meteo_web.name, meteo_web.description, meteo_api.cloud_coverage_total
from (select hour, st.name, cloud_coverage_total, description
      from hourly_forecast_tbl
               inner join forecast_tbl ft on hourly_forecast_tbl.forecast_id = ft.id
               inner join source_tbl st on st.id = ft.source_id
      where ft.source_id = 4
     ) as meteo_web
         inner join
     (select hour, st.name, cloud_coverage_total
      from hourly_forecast_tbl
               inner join forecast_tbl ft on hourly_forecast_tbl.forecast_id = ft.id
               inner join source_tbl st on st.id = ft.source_id
      where ft.source_id = 5
     ) as meteo_api
     on meteo_api.hour = meteo_web.hour
;
