select web.hour, web.name, web.description, api.cloud_coverage_total
from (select hour, st.name, cloud_coverage_total, description, location_id, date_trunc('hour', scraped) as hour_scraped
      from hourly_forecast_tbl
               inner join forecast_tbl ft on hourly_forecast_tbl.forecast_id = ft.id
               inner join source_tbl st on st.id = ft.source_id
      where ft.source_id = 4
     ) as web
         inner join
     (select hour, st.name, cloud_coverage_total, location_id, date_trunc('hour', scraped) as hour_scraped
      from hourly_forecast_tbl
               inner join forecast_tbl ft on hourly_forecast_tbl.forecast_id = ft.id
               inner join source_tbl st on st.id = ft.source_id
      where ft.source_id = 5
     ) as api
     on api.hour = web.hour
            and api.location_id = web.location_id
            and api.hour_scraped = web.hour_scraped
where description = 'icon_22'
;

select distinct description
from hourly_forecast_tbl
order by description asc;
