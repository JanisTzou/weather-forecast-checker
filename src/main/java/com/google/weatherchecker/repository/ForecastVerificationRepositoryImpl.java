package com.google.weatherchecker.repository;

import com.google.weatherchecker.model.ForecastVerification;
import com.google.weatherchecker.model.ForecastVerificationType;
import com.google.weatherchecker.model.Source;
import com.google.weatherchecker.util.Utils;
import com.google.weatherchecker.verification.PastHoursProps;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
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
                         frcst.hour,
                         msrmt.cloud_coverage_total                                                          AS measured_total,
                         frcst.cloud_coverage_total                                                          AS forecast_total,
                         abs(msrmt.cloud_coverage_total - frcst.cloud_coverage_total)                        AS diff_abs,
                         msrmt.cloud_coverage_total - frcst.cloud_coverage_total                             AS diff
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
                                                INNER JOIN county_tbl AS c ON lt.county_id = c.id
                                                     AND (CASE WHEN (:includeCounties) then c.name in (:county) ELSE true END)
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

    // TODO the last day should not be included ...
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
    private final PastHoursProps pastHoursProps;


    @Override
    public List<LocalDate> getMissingDailyVerificationDates() {
        return namedParameterJdbcTemplate.query(missingDayVerificationsDates, (rs, rowNum) ->
                rs.getDate("date").toLocalDate()
        );
    }

    @Override
    public List<ForecastVerification> findVerifications(VerificationCriteria criteria) {
        List<JpaForecastVerification> verifications;
        if (criteria.getFromDate() == null && criteria.getToDate() == null) {
            if (criteria.getPastHours() != null) {
                if (criteria.getCounties().isEmpty()) {
                    verifications = jpaForecastVerificationRepository.findAllByPastHoursAndCountyIsNull(criteria.getPastHours());
                } else {
                    verifications = jpaForecastVerificationRepository.findAllByPastHoursAndCountyNameIn(criteria.getPastHours(), criteria.getCounties());
                }

            } else if (criteria.getDate() != null) {
                if (criteria.getCounties().isEmpty()) {
                    verifications = jpaForecastVerificationRepository.findAllByDayAndCountyIsNull(criteria.getDate());
                } else {
                    verifications = jpaForecastVerificationRepository.findAllByDayAndCountyNameIn(criteria.getDate(), criteria.getCounties());
                }
            } else {
                log.warn("Unhandled case ...");
                verifications = Collections.emptyList();
            }
        } else {
            if (criteria.getCounties().isEmpty()) {
                verifications = jpaForecastVerificationRepository.findAllByDayBetweenAndCountyIsNullOrderByDay(criteria.getFromDate(), criteria.getToDate());
            } else {
                verifications = jpaForecastVerificationRepository.findAllByDayBetweenAndCountyNameInOrderByDay(criteria.getFromDate(), criteria.getToDate(), criteria.getCounties());
            }
        }
        return verifications.stream()
                .map(jpaForecastVerificationMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<ForecastVerification> calculateVerifications(VerificationCriteria criteria) {
        if (criteria.getCounties().size() > 1) {
            throw new IllegalArgumentException("Not allowed to calculate verifications for multiple counties");
        }
        Map<String, Object> params = criteria.toParamsMap();
        String sql = Utils.fillTemplate(forecastVsMeasurementForPastHoursSql, params);
        SqlParameterSource parameters = new MapSqlParameterSource(params);

        // TODO maybe we want different ForecastVerifications for hourly vs daily verification ... date range ...
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
                criteria.getCounties().stream().findFirst().orElse(null)
        ));
    }

    @Transactional
    @Override
    public void save(List<ForecastVerification> verifications) {
        for (ForecastVerification verification : verifications) {
            save(verification);
        }
        log.info("Saved {} verifications", verifications.size());
    }

    @Transactional
    @Override
    public void save(ForecastVerification verification) {
        log.trace("Saving verification: {}", verification);
        JpaForecastVerification jpaVerification = jpaForecastVerificationMapper.toEntity(verification);
        Optional<JpaSource> jpaSource = jpaSourceRepository.findFirstByName(verification.getSource());

        String county = verification.getCounty();
        Optional<JpaCounty> jpaCounty = county != null ? jpaCountyRepository.findFirstByName(county) : Optional.empty();

        Optional<JpaForecastVerificationType> verificationType = jpaForecastVerificationTypeRepository.findFirstByName(verification.getType());

        if (jpaSource.isPresent()
                && (county == null || jpaCounty.isPresent())
                && (verificationType.isPresent())
        ) {
            jpaVerification.setSource(jpaSource.get());
            jpaVerification.setCounty(jpaCounty.orElse(null));
            jpaVerification.setType(verificationType.get());
            jpaForecastVerificationRepository.save(jpaVerification);
        } else {
            log.error("Failed to save verification: {}", verification);
        }
    }


    @Override
    public void createDailyVerifications() {
        List<LocalDate> missingDays = getMissingDailyVerificationDates();

        for (LocalDate date : missingDays) {
            queryAndSave(VerificationCriteria.builder().setDate(date).build());
        }

        for (JpaCounty jpaCounty : jpaCountyRepository.findAll()) {
            for (LocalDate date : missingDays) {
                queryAndSave(VerificationCriteria.builder().addCounty(jpaCounty.getName()).setDate(date).build());
            }
        }
    }

    @Override
    public void updatePastHoursVerifications() {

        List<Integer> pastHours = pastHoursProps.getPastHours();

        List<Integer> idsToRemove = jpaForecastVerificationRepository.findAllIdsByType(ForecastVerificationType.PAST_N_HOURS);

        // whole Czech republic ...
        for (Integer hours : pastHours) {
            jpaForecastVerificationRepository.deleteAllByPastHoursAndCountyName(hours, null);
            queryAndSave(VerificationCriteria.builder().setPastHours(hours).build());
        }

        // by county
        for (JpaCounty county : jpaCountyRepository.findAll()) {
            for (Integer hours : pastHours) {
                jpaForecastVerificationRepository.deleteAllByPastHoursAndCountyName(hours, county.getName());
                queryAndSave(VerificationCriteria.builder().setPastHours(hours).addCounty(county.getName()).build());
            }
        }

        // in case the hour list changed, there might be some left-overs to delete ...
        for (Integer id : idsToRemove) {
            if (jpaForecastVerificationRepository.existsById(id)) {
                log.info("Removing leftover verification");
                jpaForecastVerificationRepository.deleteById(id);
            }
        }

    }

    private void queryAndSave(VerificationCriteria criteria) {
        List<ForecastVerification> verifications = calculateVerifications(criteria);
        if (verifications.isEmpty()) {
            log.info("Got no verifications for: {}", criteria);
        }
        save(verifications);
    }

}
