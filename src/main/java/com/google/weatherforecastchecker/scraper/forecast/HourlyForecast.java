package com.google.weatherforecastchecker.scraper.forecast;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class HourlyForecast {

    private final LocalDateTime hour;
    private final Integer cloudCoverTotal;
    private final String description;

}
