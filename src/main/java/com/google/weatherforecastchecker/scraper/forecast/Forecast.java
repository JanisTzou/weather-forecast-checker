package com.google.weatherforecastchecker.scraper.forecast;

import lombok.Data;

import java.util.List;

@Data
public class Forecast {

    private final Source source;
    private final String location;
    // TODO for meteoblue API ...
//    private final LocalDateTime modelRun;
//    private final LocalDateTime modelRunUpdate;
    private final List<HourForecast> hourForecasts;

}
