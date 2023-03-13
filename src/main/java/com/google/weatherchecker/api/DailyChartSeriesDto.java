package com.google.weatherchecker.api;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class DailyChartSeriesDto {

    private String source;
    private String title;
    private List<DailyChartValueDto> values;

}
