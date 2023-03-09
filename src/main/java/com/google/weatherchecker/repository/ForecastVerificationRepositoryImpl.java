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
            SELECT source,
                   round(avg(measured_total)) AS avg_measured_total,
                   round(avg(forecast_total)) AS avg_forecast_total,
                   round(avg(diff_abs)) AS avg_diff_abs,
                   round(avg(diff))     AS avg_diff,
                   count(hour)          AS record_count
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
                        WHERE hft.hour >= ft.scraped -- ensures that we do not include "forecast of the past"
                            AND (CASE
                                   WHEN (:includePastHours) then hft.hour >= now() - interval ${pastHours} AND hft.hour <= now()
                                   WHEN (:includeDateBounds) then hft.hour >= '${fromDateTime}' AND hft.hour <= '${toDateTime}'
                                   ELSE true
                            END)
                            -- for another usecase we want whole past days intervals
                        GROUP BY ft.source_id, st.name, ft.location_id, hft.hour
                       ) AS frcst
                           INNER JOIN (SELECT max(ccm.id),
                                                      ccm.location_id,
                                                      lt.name,
                                                      ccm.date_time,
                                                      avg(ccm.cloud_coverage_total) AS cloud_coverage_total,
                                                      date_trunc('hour', max(ccm.scraped)) AS scraped_hour_dt
                                       FROM cloud_coverage_measurement_tbl AS ccm
                                                INNER JOIN location_tbl AS lt ON ccm.location_id = lt.id
                                                INNER JOIN region_tbl AS r ON lt.region_id = r.id
                                                     AND (CASE WHEN (:includeRegion) then r.name in (:region) ELSE true END)
                                                INNER JOIN county_tbl AS c ON lt.county_id = c.id
                                                     AND (CASE WHEN (:includeCounty) then c.name in (:county) ELSE true END)
                                       WHERE source_id = 6
                                         AND ccm.cloud_coverage_total IS NOT NULL
                                       GROUP BY ccm.location_id, lt.name, ccm.date_time
                                      ) AS msrmt ON frcst.location_id = msrmt.location_id
                      AND frcst.hour = msrmt.date_time
                 ) comparison
            GROUP BY comparison.source
            ORDER BY avg_diff_abs
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
