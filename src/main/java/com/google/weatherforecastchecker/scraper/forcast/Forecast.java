package com.google.weatherforecastchecker.scraper.forcast;

import lombok.Data;

import java.util.List;

@Data
public class Forecast {

    private final String location;
    private final List<HourForecast> hourForecasts;

}
