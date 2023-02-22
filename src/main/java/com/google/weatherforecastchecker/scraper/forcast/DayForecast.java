package com.google.weatherforecastchecker.scraper.forcast;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class DayForecast {

    private final String locationName;
    private final LocalDateTime dateTime;
    // 24 values expected but there can be an exception
    private final List<Integer> cloudCoverPercentage;
    // 24 values expected
    private final List<String> descriptions;

}
