package com.google.weatherchecker.api;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
public class SourcesComparisonDto {

    private final Integer pastHours;
    private final String region;
    private final String county;
    private final LocalDate date;

    private List<ForecastVerificationDto> verifications;

}
