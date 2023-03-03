package com.google.weatherchecker.model;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@RequiredArgsConstructor
public class ForecastVerification {

    private final LocalDateTime created;
    private final ForecastVerificationType type;
    private final Source source;
    private final int avgForecastCloudTotal;
    private final int avgMeasuredCloudTotal;
    private final int avgDiffAbs;
    private final int avgDiff;
    private final int recordCount;
    private final Integer pastHours;
    private final LocalDate day;
    protected final String region;
    protected final String county;

}
