package com.google.weatherchecker.repository;

import com.google.weatherchecker.util.Utils;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Log4j2
@Repository
@RequiredArgsConstructor
public class ForecastAnalysisRepositoryImpl implements ForecastAnalysisRepository {

    private static final String forecastVsMeasurementForPastHoursSql = """
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
                                   when (:includePastHours) then hft.hour >= now() - interval ${pastHours} and hft.hour <= now()
                                   when (:includeDateBounds) then hft.hour >= '${fromDateTime}' and hft.hour <= '${toDateTime}'
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
                                                                            when (:includeRegion) then l.region in (:region) 
                                                                            when (:includeCounty) then l.county in (:county) 
                                                                            else true
                                                                         end)
                                       where source_id = 6
                                         and ccm.cloud_coverage_total is not null) as msrmt on frcst.location_id = msrmt.location_id
                      and frcst.hour = msrmt.date_time
                 ) comparison
            group by comparison.source
            order by avg_diff_abs
            ;
            """;

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Override
    public List<Comparison> query(Criteria criteria) {
        Map<String, Object> params = criteria.toParamsMap();
        SqlParameterSource parameters = new MapSqlParameterSource(params);

        String sql = Utils.fillTemplate(forecastVsMeasurementForPastHoursSql, params);

        return namedParameterJdbcTemplate.query(sql, parameters, (rs, rowNum) -> new Comparison(
                rs.getString("source"),
                rs.getInt("avg_diff_abs"),
                rs.getInt("avg_diff"),
                rs.getInt("record_count"),
                rs.getString("forecast_description"),
                rs.getString("forecast_error_description")
        ));
    }

}
