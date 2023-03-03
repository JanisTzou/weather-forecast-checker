package com.google.weatherchecker.api;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class ChartSeriesDto {

    private String source;
    private List<ChartValueDto> values;

}
