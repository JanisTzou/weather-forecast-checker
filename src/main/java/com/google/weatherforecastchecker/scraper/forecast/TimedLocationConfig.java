package com.google.weatherforecastchecker.scraper.forecast;

import com.google.weatherforecastchecker.scraper.LocationConfig;
import lombok.Data;

import java.time.LocalTime;

@Data
public class TimedLocationConfig<T extends LocationConfig> {
    private final LocalTime scrapingTime;
    private final T locationConfig;
}
