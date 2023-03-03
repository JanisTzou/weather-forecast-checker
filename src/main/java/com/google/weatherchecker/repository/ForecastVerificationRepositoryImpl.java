package com.google.weatherchecker.repository;

import com.google.weatherchecker.model.ForecastVerification;
import com.google.weatherchecker.model.ForecastVerificationType;
import com.google.weatherchecker.model.Source;
import com.google.weatherchecker.util.Utils;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Log4j2
@Repository
@RequiredArgsConstructor
public class ForecastVerificationRepositoryImpl implements ForecastVerificationRepository {

    private static final String forecastVsMeasurementForPastHoursSql = """
            select source,
                   round(avg(measured_total)) avg_measured_total,
                   round(avg(forecast_total)) avg_forecast_total,
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
                         frcst.hour,
                         msrmt.date_time,
                         msrmt.cloud_coverage_total                                                          as measured_total,
                         frcst.cloud_coverage_total                                                          as forecast_total,
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
                           inner join (select max(ccm.id),
                                                      ccm.location_id,
                                                      l.name,
                                                      ccm.date_time,
                                                      avg(ccm.cloud_coverage_total) as cloud_coverage_total,
                                                      date_trunc('hour', max(ccm.scraped)) as scraped_hour_dt
                                       from cloud_coverage_measurement_tbl as ccm
                                                inner join location_tbl as l on ccm.location_id = l.id
                                                inner join region_tbl as r on l.region_id = r.id
                                                     and (case when (:includeRegion) then r.name in (:region) else true end)
                                                inner join county_tbl as c on l.county_id = c.id
                                                     and (case when (:includeCounty) then c.name in (:county) else true end)
                                       where source_id = 6
                                         and ccm.cloud_coverage_total is not null
                                       group by ccm.location_id, l.name, ccm.date_time
                                      ) as msrmt on frcst.location_id = msrmt.location_id
                      and frcst.hour = msrmt.date_time
                 ) comparison
            group by comparison.source
            order by avg_diff_abs
            ;
            """;

    private static final String missingDayVerificationsDates = """
                    SELECT DISTINCT DATE(ft.scraped - INTERVAL '1 day') AS date
                    FROM forecast_tbl ft
                    WHERE DATE(ft.scraped - INTERVAL '1 day') NOT IN
                          (SELECT DISTINCT day
                           FROM forecast_verification_tbl
                           WHERE day IS NOT NULL)
                    ORDER BY date;
            """;

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    private final JpaForecastVerificationRepository jpaForecastVerificationRepository;
    private final JpaForecastVerificationMapper jpaForecastVerificationMapper;
    private final JpaSourceRepository jpaSourceRepository;
    private final JpaForecastVerificationTypeRepository jpaForecastVerificationTypeRepository;
    private final JpaRegionRepository jpaRegionRepository;
    private final JpaCountyRepository jpaCountyRepository;


    @Override
    public List<LocalDate> getMissingDayVerificationDates() {
        return namedParameterJdbcTemplate.query(missingDayVerificationsDates, (rs, rowNum) ->
                rs.getDate("date").toLocalDate()
        );
    }

    @Override
    public void deleteAllByType(ForecastVerificationType type) {
        jpaForecastVerificationRepository.deleteAllByTypeName(type);
    }

    @Override
    public List<ForecastVerification> findVerifications(Criteria criteria) {
        return jpaForecastVerificationRepository.findAllByPastHoursAndDayAndCountyNameAndRegionName(
                        criteria.getPastHours(),
                        criteria.getDate(),
                        criteria.getCounty(),
                        criteria.getRegion()
                )
                .stream()
                .map(jpaForecastVerificationMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<ForecastVerification> calculateVerifications(Criteria criteria) {
        Map<String, Object> params = criteria.toParamsMap();
        SqlParameterSource parameters = new MapSqlParameterSource(params);

        String sql = Utils.fillTemplate(forecastVsMeasurementForPastHoursSql, params);

        return namedParameterJdbcTemplate.query(sql, parameters, (rs, rowNum) -> new ForecastVerification(
                LocalDateTime.now(),
                criteria.getVerificationType(),
                Source.valueOf(rs.getString("source")),
                rs.getInt("avg_forecast_total"),
                rs.getInt("avg_measured_total"),
                rs.getInt("avg_diff_abs"),
                rs.getInt("avg_diff"),
                rs.getInt("record_count"),
                criteria.getPastHours(),
                criteria.getDate(),
                criteria.getRegion(),
                criteria.getCounty()
        ));
    }

    @Transactional
    @Override
    public void save(List<ForecastVerification> verifications) {
        for (ForecastVerification verification : verifications) {
            save(verification);
        }
    }

    @Transactional
    @Override
    public void save(ForecastVerification verification) {
        log.info("Saving verification: {}", verification); // TODO trace ...
        JpaForecastVerification jpaVerification = jpaForecastVerificationMapper.toEntity(verification);
        Optional<JpaSource> jpaSource = jpaSourceRepository.findFirstByName(verification.getSource());
        String region = verification.getRegion();
        Optional<JpaRegion> jpaRegion = region != null ? jpaRegionRepository.findFirstByName(region) : Optional.empty();

        String county = verification.getCounty();
        Optional<JpaCounty> jpaCounty = county != null ? jpaCountyRepository.findFirstByName(county) : Optional.empty();

        Optional<JpaForecastVerificationType> verificationType = jpaForecastVerificationTypeRepository.findFirstByName(verification.getType());

        if (jpaSource.isPresent()
                && (region == null || jpaRegion.isPresent())
                && (county == null || jpaCounty.isPresent())
                && (verificationType.isPresent())
        ) {
            jpaVerification.setSource(jpaSource.get());
            jpaVerification.setRegion(jpaRegion.orElse(null));
            jpaVerification.setCounty(jpaCounty.orElse(null));
            jpaVerification.setType(verificationType.get());
            jpaForecastVerificationRepository.save(jpaVerification);
        } else {
            log.error("Failed to save verification: {}", verification);
        }
    }


    @Override
    public void createDailyVerifications() {
        List<LocalDate> missingDays = getMissingDayVerificationDates();

        for (LocalDate date : missingDays) {
            queryAndSave(Criteria.from(null, null, null, date));
        }

        for (JpaCounty jpaCounty : jpaCountyRepository.findAll()) {
            for (LocalDate date : missingDays) {
                queryAndSave(Criteria.from(null, null, jpaCounty.getName(), date));
            }
        }

        // regions
        for (JpaRegion jpaRegion : jpaRegionRepository.findAll()) {
            for (LocalDate date : missingDays) {
                queryAndSave(Criteria.from(null, jpaRegion.getName(), null, date));
            }
        }
    }

    @Override
    public void updatePastHoursVerifications() {

        // TODO have this somewhere configured ...
        List<Integer> pastHours = List.of(12, 24, 36);

        List<Integer> idsToRemove = jpaForecastVerificationRepository.findAllIdsByType(ForecastVerificationType.PAST_N_HOURS);

        // whole Czech republic ...
        for (Integer hours : pastHours) {
            jpaForecastVerificationRepository.deleteAllByPastHoursAndCountyNameAndRegionName(hours, null, null);
            queryAndSave(Criteria.from(hours, null, null, null));
        }

        // by county
        for (JpaCounty jpaCounty : jpaCountyRepository.findAll()) {
            for (Integer hours : pastHours) {
                jpaForecastVerificationRepository.deleteAllByPastHoursAndCountyNameAndRegionName(hours, jpaCounty.getName(), null);
                queryAndSave(Criteria.from(hours, null, jpaCounty.getName(), null));
            }
        }

        // regions
        for (JpaRegion jpaRegion : jpaRegionRepository.findAll()) {
            for (Integer hours : pastHours) {
                jpaForecastVerificationRepository.deleteAllByPastHoursAndCountyNameAndRegionName(hours, null, jpaRegion.getName());
                queryAndSave(Criteria.from(hours, jpaRegion.getName(), null, null));
            }
        }

        // in case the hour list changed, there might be some left-overs to delete ...
        for (Integer id : idsToRemove) {
            // TODO wrap in try catch ...
            if (jpaForecastVerificationRepository.existsById(id)) {
                log.info("Removing leftover verification");
                jpaForecastVerificationRepository.deleteById(id);
            }
        }
    }

    private void queryAndSave(Criteria criteria) {
        List<ForecastVerification> verifications = calculateVerifications(criteria);
        save(verifications);
    }


}
