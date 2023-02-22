package com.google.weatherforecastchecker.scraper.forcast;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class HourForecast {

    private final LocalDateTime hour;
    // 24 values expected but there can be an exception
    private final int cloudCoverPercentage;
    // 24 values expected
    private final String description;

}
