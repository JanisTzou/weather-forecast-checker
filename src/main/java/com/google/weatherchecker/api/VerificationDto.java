package com.google.weatherchecker.api;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class VerificationDto {

    private String source;
    private int avgForecastCloudTotal;
    private int avgMeasuredCloudTotal;
    private int avgDiffAbs;
    private int avgDiff;
    private int recordCount;
    private String forecastDesc;
    private String forecastErrorDesc;

}
