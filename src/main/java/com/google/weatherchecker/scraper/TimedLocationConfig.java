package com.google.weatherchecker.scraper;

import lombok.Data;

import java.time.LocalTime;

@Data
public class TimedLocationConfig<T extends LocationConfig> {
    private final LocalTime scrapingTime;
    private final T locationConfig;
}
