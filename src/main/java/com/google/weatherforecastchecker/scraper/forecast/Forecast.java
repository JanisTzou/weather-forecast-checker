package com.google.weatherforecastchecker.scraper.forecast;

import lombok.Data;

import java.util.List;

@Data
public class Forecast {

    private final Source source;
    private final String location;
    private final List<HourForecast> hourForecasts;

}
