package com.google.weatherforecastchecker.scraper.forecast;

import com.google.weatherforecastchecker.scraper.Location;
import com.google.weatherforecastchecker.scraper.Source;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class Forecast {

    private final LocalDateTime scraped;
    private final Source source;
    private final Location location;
    // TODO for meteoblue API ...
//    private final LocalDateTime modelRun;
//    private final LocalDateTime modelRunUpdate;
    private final List<HourlyForecast> hourlyForecasts;

}
