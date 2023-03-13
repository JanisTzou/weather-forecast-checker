package com.google.weatherchecker.api;

import lombok.Data;

import java.util.List;

@Data
public class LongtermSourceComparisonChartDto {

    private final String title;
    private final List<LongtermComparisonValueDto> values;

}
