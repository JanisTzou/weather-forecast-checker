package com.google.weatherchecker.api;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ComparisonDto {

    private String source;
    private int avgDiffAbs;
    private int avgDiff;
    private int recordCount;
    private String forecastDescription;
    private String forecastErrorDescription;

}
