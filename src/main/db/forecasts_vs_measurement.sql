select measure.id,
       measure.name,
       measure.date_time as hour,
       meteo_web.scraped_dt,
       measure.cloud_coverage_total   as measured,
       meteo_web.cloud_coverage_total - measure.cloud_coverage_total as meteo_web_diff,
       aladin_api.cloud_coverage_total - measure.cloud_coverage_total as aladin_diff,
       clearoutside.cloud_coverage_total - measure.cloud_coverage_total as clearoutside_diff
--        accu.cloud_coverage_total - measure.cloud_coverage_total as accuweather_diff
from (select ccm.id, location_id, name, date_time, cloud_coverage_total, date_trunc('hour', scraped) as scraped_dt
      from cloud_coverage_measurement_tbl as ccm
               inner join location_tbl as l on ccm.location_id = l.id
      where source_id = 6
        and ccm.cloud_coverage_total is not null) as measure
         left join (select hour, st.name, cloud_coverage_total, location_id, date_trunc('hour', scraped) as scraped_dt
                    from hourly_forecast_tbl hft
                             inner join forecast_tbl ft on hft.forecast_id = ft.id
                             inner join source_tbl st on st.id = ft.source_id
                    where ft.source_id = 4) as meteo_web
                   on measure.location_id = meteo_web.location_id
                       and measure.date_time = meteo_web.hour
                       and measure.date_time = (meteo_web.scraped_dt + interval '6 hours' )
         left join (select hour, st.name, cloud_coverage_total, location_id, date_trunc('hour', scraped) as scraped_dt
                    from hourly_forecast_tbl hft
                             inner join forecast_tbl ft on hft.forecast_id = ft.id
                             inner join source_tbl st on st.id = ft.source_id
                    where ft.source_id = 2) as aladin_api
                   on measure.location_id = aladin_api.location_id
                       and measure.date_time = aladin_api.hour
                       and measure.date_time = (aladin_api.scraped_dt + interval '6 hours' )
         left join (select hour, st.name, cloud_coverage_total, location_id, date_trunc('hour', scraped) as scraped_dt
                    from hourly_forecast_tbl hft
                             inner join forecast_tbl ft on hft.forecast_id = ft.id
                             inner join source_tbl st on st.id = ft.source_id
                    where ft.source_id = 3) as clearoutside
                   on measure.location_id = clearoutside.location_id
                       and measure.date_time = clearoutside.hour
                       and measure.date_time = (clearoutside.scraped_dt + interval '6 hours' )
--          left join (select hour, st.name, cloud_coverage_total, location_id, date_trunc('hour', scraped) as scraped_dt
--                     from hourly_forecast_tbl hft
--                              inner join forecast_tbl ft on hft.forecast_id = ft.id
--                              inner join source_tbl st on st.id = ft.source_id
--                     where ft.source_id = 1) as accu
--                    on measure.location_id = accu.location_id
--                        and measure.date_time = accu.hour
--                        and measure.date_time = (accu.scraped_dt + interval '5 hours' )
where meteo_web.cloud_coverage_total is not null
  and aladin_api.cloud_coverage_total is not null
  and clearoutside.cloud_coverage_total is not null
--   and accu.cloud_coverage_total is not null
;



select hour, st.name, cloud_coverage_total, location_id, date_trunc('hour', scraped) as forecast_scraped
from hourly_forecast_tbl
         inner join forecast_tbl ft on hourly_forecast_tbl.forecast_id = ft.id
         inner join source_tbl st on st.id = ft.source_id
where ft.source_id = 4
;


select hour, source_id, date_trunc('hour', scraped) as scraped
from hourly_forecast_tbl
         inner join forecast_tbl ft on hourly_forecast_tbl.forecast_id = ft.id
where  source_id = 1
;

select *
from source_tbl;


select source, avg(diff_abs) avg_diff_abs, count(hour) record_count
from (select frcst.name                                                                 as source,
             msrmt.name                                                                 as location,
             frcst.scraped_dt                                                           as forecast_scraped_dt,
             frcst.scraped_hour_dt                                                      as forecast_scraped_hour_dt,
             hour,
             msrmt.cloud_coverage_total                                                 as measured,
             frcst.cloud_coverage_total                                                 as forecast,
             abs(msrmt.cloud_coverage_total - frcst.cloud_coverage_total)               as diff_abs,
             msrmt.cloud_coverage_total - frcst.cloud_coverage_total                    as diff,
             extract(epoch from (msrmt.scraped_hour_dt - frcst.scraped_hour_dt)) / 3600 as hours_after_forecast
from(select hour,st.name,cloud_coverage_total,location_id,date_trunc('hour', scraped) as scraped_hour_dt,scraped scraped_dt
     from hourly_forecast_tbl hft
              inner join forecast_tbl ft on hft.forecast_id = ft.id
              inner join source_tbl st on st.id = ft.source_id
     where
--            ft.source_id = 3 and
           hft.hour > ft.scraped
    and ft.scraped > '2023-02-26 12:00:00.000000'
    ) as frcst
inner join (select ccm.id, location_id, name, date_time, cloud_coverage_total, date_trunc('hour', scraped) as scraped_hour_dt
            from cloud_coverage_measurement_tbl as ccm
                     inner join location_tbl as l on ccm.location_id = l.id
            where source_id = 6
              and ccm.cloud_coverage_total is not null
    ) as msrmt on frcst.location_id = msrmt.location_id and frcst.hour = msrmt.date_time
    and (extract(epoch from (msrmt.scraped_hour_dt - frcst.scraped_hour_dt)) / 3600) <= 12 -- restrict hours after forecast
--     here we can also have conditions like 6 hours after the forecast was scraped etc ...
) comparison
group by comparison.source
;
