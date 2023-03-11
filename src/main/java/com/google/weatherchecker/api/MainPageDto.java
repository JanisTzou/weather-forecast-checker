package com.google.weatherchecker.api;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class MainPageDto {

    private final String region;
    private final String county;
    private final List<PastHoursDto> pastHoursDtos;

    private final ChartDto chartDto;

}
