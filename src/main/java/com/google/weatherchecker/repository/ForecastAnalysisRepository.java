package com.google.weatherchecker.repository;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface ForecastAnalysisRepository {

    List<Comparison> query(Criteria criteria);

    @Data
    @RequiredArgsConstructor
    public static class Comparison {
        private final String source;
        private final int avgDiffAbs;
        private final int avgDiff;
        private final int records;
        private final String forecastDescription;
        private final String forecastErrorDescription;
    }

    @RequiredArgsConstructor
    public static class Criteria {

        private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        private final Integer pastHours;
        private final String region;
        private final String county;
        private final LocalDate date;

        public static Criteria from(Integer pastHours, String region, String county, LocalDate date) {
            return new Criteria(pastHours, region, county, date);
        }

        public static Criteria forPastHoursAndRegion(Integer pastHours, String region) {
            return new Criteria(pastHours, region, null, null);
        }

        public static Criteria forPastHoursAndCounty(Integer pastHours, String county) {
            return new Criteria(pastHours, null, county, null);
        }

        public static Criteria forDateAndCounty(LocalDate date, String county) {
            return new Criteria(null, null, county, date);
        }

        public static Criteria forDateAndRegion(LocalDate date, String region) {
            return new Criteria(null, region, null, date);
        }

        public static Criteria forDate(LocalDate date) {
            return new Criteria(null, null, null, date);
        }

        public static Criteria forPastHours(Integer pastHours) {
            return new Criteria(pastHours, null, null, null);
        }

        public Map<String, Object> toParamsMap() {
            Map<String, Object> params = new HashMap<>();

            // pastHours are special ... regular named parameters will not work for it as its enclosed in single quotes ... so we need to use different tempating
            boolean includePastHours = pastHours != null;
            params.put("includePastHours", includePastHours);
            if (pastHours != null) {
                if (pastHours == 1) {
                    params.put("pastHours", "'" + pastHours + " hour'");
                } else {
                    params.put("pastHours", "'" + pastHours + " hours'");
                }
            } else {
                params.put("pastHours", "'0 hours'"); // we need to provide somethig for valid sql ...
            }

            // region
            boolean includeRegion = region != null;
            params.put("includeRegion", includeRegion);
            String regionParam = includeRegion ? region : "-";
            params.put("region", regionParam);

            // county
            boolean includeCounty = county != null;
            params.put("includeCounty", includeCounty);
            String countyParam = includeCounty ? county : "-";
            params.put("county", countyParam);

            // date bounds
            boolean includeDateBounds = date != null;
            params.put("includeDateBounds", includeDateBounds);
            LocalDateTime fromParam = includeDateBounds ? LocalDateTime.of(date, LocalTime.MIN) : LocalDateTime.now();
            params.put("fromDateTime", DATE_TIME_FORMATTER.format(fromParam));

            // 2023-02-28 00:00:00
            LocalDateTime toParam = includeDateBounds ? LocalDateTime.of(date, LocalTime.MAX) : LocalDateTime.now();
            params.put("toDateTime", DATE_TIME_FORMATTER.format(toParam));

            return params;
        }

    }
}
