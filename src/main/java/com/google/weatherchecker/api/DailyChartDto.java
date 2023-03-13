package com.google.weatherchecker.api;

import lombok.Data;

import java.util.List;

@Data
public class DailyChartDto {

    private final String title;
    private final List<DailyChartSeriesDto> series;

}