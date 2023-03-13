package com.google.weatherchecker.api;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class MainPageDto {

    private final String title;
    private final List<String> counties;
    private final List<PastHoursDto> pastHours;
    private final DailyChartDto dailyChart;
    private final LongtermSourceComparisonChartDto longtermComparisonChart;

}
