package com.google.weatherforecastchecker.scraper.forecast;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class HourForecast {

    private final LocalDateTime hour;
    // 24 values expected but there can be an exception
    private final int cloudCoverPercentage;
    // 24 values expected
    private final String description;

}
