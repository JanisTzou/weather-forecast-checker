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

select source,
       round(avg(diff_abs)) avg_diff_abs,
       round(avg(diff))     avg_diff,
       count(hour)          record_count,
       case
           when avg(diff_abs) > 20 then 'very bad'
           when avg(diff_abs) > 13 then 'bad'
           when avg(diff_abs) > 7 then 'good'
           else 'excellent'
           end as           forecast_description,
       case
           when avg(diff) > 20 then 'very optimistic'
           when avg(diff) > 5 then 'optimistic'
           when avg(diff) >= -5 then 'mixed'
           when avg(diff) >= -20 then 'pessimistic'
           else 'very pessimistic'
           end as           forecast_error_description
from (select frcst.source_name                                                                   as source,
             msrmt.name                                                                          as location,
             frcst.forecast_scraped_dt                                                           as forecast_scraped_dt,
             frcst.forecast_scraped_hour_dt                                                      as forecast_scraped_hour_dt,
             hour,
             msrmt.cloud_coverage_total                                                          as measured,
             frcst.cloud_coverage_total                                                          as forecast,
             abs(msrmt.cloud_coverage_total - frcst.cloud_coverage_total)                        as diff_abs,
             msrmt.cloud_coverage_total - frcst.cloud_coverage_total                             as diff,
             extract(epoch from (msrmt.scraped_hour_dt - frcst.forecast_scraped_hour_dt)) / 3600 as hours_after_forecast
      from (select ft.source_id,
                   st.name                             as source_name,
                   ft.location_id,
                   hft.hour,
                   max(hft.id)                         as hour_id,
                   max(ft.id)                          as forecast_id,
                   date_trunc('hour', max(ft.scraped)) as forecast_scraped_hour_dt,
                   max(ft.scraped)                     as forecast_scraped_dt,
                   max(hft.cloud_coverage_total)       as cloud_coverage_total
            from hourly_forecast_tbl hft
                     inner join forecast_tbl ft on hft.forecast_id = ft.id
                     inner join source_tbl st on st.id = ft.source_id
            where hft.hour >= ft.scraped -- ensures that we do not include "forecast of the past"
              and (case
                       when (true) then hft.hour >= now() - interval '36 hours' and hft.hour <= now()
                       when (false) then hft.hour >= '2023-02-28 00:00:00' and hft.hour <= '2023-02-28 23:59:59'
                       else true
                end)
                -- for another usecase we want whole past days intervals
            group by ft.source_id, st.name, ft.location_id, hft.hour
           ) as frcst
               inner join (select ccm.id,
                                  location_id,
                                  name,
                                  date_time,
                                  cloud_coverage_total,
                                  date_trunc('hour', scraped) as scraped_hour_dt
                           from cloud_coverage_measurement_tbl as ccm
                                    inner join location_tbl as l on ccm.location_id = l.id
                               and (case
                                        when (false) then l.region in ('Střední Čechy', 'Praha')
                                         when (false) then l.county in ('Středočeský kraj', 'Hlavní město Praha')
                                        else true
                                   end)
                           where source_id = 6
                             and ccm.cloud_coverage_total is not null) as msrmt on frcst.location_id = msrmt.location_id
          and frcst.hour = msrmt.date_time
     ) comparison
group by comparison.source
order by avg_diff_abs
;
