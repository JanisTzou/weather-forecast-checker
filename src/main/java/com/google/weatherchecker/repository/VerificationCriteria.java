package com.google.weatherchecker.repository;

import com.google.weatherchecker.model.ForecastVerificationType;
import lombok.Getter;
import lombok.ToString;
import org.apache.commons.lang3.Validate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Getter
@ToString
public
class VerificationCriteria {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final Integer pastHours;
    private final String region;
    private final String county;
    private final LocalDate date;
    private final LocalDate fromDate;
    private final LocalDate toDate;

    private VerificationCriteria(Integer pastHours, String region, String county, LocalDate date, LocalDate fromDate, LocalDate toDate) {
        Validate.isTrue(pastHours != null || date != null || (fromDate != null && toDate != null), "Either pastHours or date or date range must be specified");
        Validate.isTrue(!(region != null && county != null), "Either region or county can be specified, not both");
//            Validate.isTrue(!(pastHours != null && date != null), "Either pastHours or date can be specified, not both");
        this.pastHours = pastHours;
        this.region = region;
        this.county = county;
        this.date = date;
        // TODO add verification that date is not set if these two are ...
        this.fromDate = fromDate;
        this.toDate = toDate;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static VerificationCriteria from(Integer pastHours, String region, String county, LocalDate date, LocalDate fromDate, LocalDate toDate) {
        return new VerificationCriteria(pastHours, region, county, date, fromDate, toDate);
    }

    public ForecastVerificationType getVerificationType() {
        if (pastHours != null) {
            return ForecastVerificationType.PAST_N_HOURS;
        } else if (date != null) {
            return ForecastVerificationType.DAILY;
        }
        return ForecastVerificationType.ALL_TIME;
    }

    public Map<String, Object> toParamsMap() {
        Map<String, Object> params = new HashMap<>();
        addPastHours(params);
        addRegion(params);
        addCounty(params);
        addDateBounds(params);
        return params;
    }

    private void addPastHours(Map<String, Object> params) {
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
    }

    private void addRegion(Map<String, Object> params) {
        // region
        boolean includeRegion = region != null;
        params.put("includeRegion", includeRegion);
        String regionParam = includeRegion ? region : "-";
        params.put("region", regionParam);
    }

    private void addCounty(Map<String, Object> params) {
        boolean includeCounty = county != null;
        params.put("includeCounty", includeCounty);
        String countyParam = includeCounty ? county : "-";
        params.put("county", countyParam);
    }

    private void addDateBounds(Map<String, Object> params) {
        boolean include;
        LocalDateTime from;
        LocalDateTime to;

        if (date != null) {
            include = true;
            from = LocalDateTime.of(date, LocalTime.MIN);
            to = LocalDateTime.of(date, LocalTime.MAX);
        } else if (fromDate != null && toDate != null) {
            include = true;
            from = LocalDateTime.of(fromDate, LocalTime.MIN);
            to = LocalDateTime.of(toDate, LocalTime.MAX);
        } else {
            include = false;
            // some values need to be present ...
            from = LocalDateTime.now();
            to = LocalDateTime.now();
        }

        params.put("includeDateBounds", include);
        params.put("fromDateTime", DATE_TIME_FORMATTER.format(from));
        params.put("toDateTime", DATE_TIME_FORMATTER.format(to));
    }

    public static class Builder {

        private Integer pastHours;
        private String region;
        private String county;
        private LocalDate date;
        private LocalDate fromDate;
        private LocalDate toDate;

        public Builder setPastHours(Integer pastHours) {
            this.pastHours = pastHours;
            return this;
        }

        public Builder setRegion(String region) {
            this.region = region;
            return this;
        }

        public Builder setCounty(String county) {
            this.county = county;
            return this;
        }

        public Builder setDate(LocalDate date) {
            this.date = date;
            return this;
        }

        public Builder setFromToDate(LocalDate fromDate, LocalDate toDate) {
            this.fromDate = fromDate;
            this.toDate = toDate;
            return this;
        }

        public VerificationCriteria build() {
            return new VerificationCriteria(pastHours, region, county, date, fromDate, toDate);
        }
    }

}
