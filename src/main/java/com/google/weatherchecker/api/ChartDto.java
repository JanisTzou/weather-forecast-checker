package com.google.weatherchecker.api;

import lombok.Data;

import java.util.List;

@Data
public class ChartDto {

    private final String title;
    private final List<ChartSeriesDto> grapValuesList;

}
