package com.google.weatherforecastchecker.scraper.forecast;

import lombok.Data;

import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Data
abstract class ScrapingProperties {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("H:mm");

    private String url;
    private int days;
    private String scrapeAtTimes;
    private Duration delayBetweenRequests;

    public List<LocalTime> getscrapeAtTimes() {
        if (scrapeAtTimes != null) {
            return Arrays.stream(scrapeAtTimes.split(","))
                    .map(text -> LocalTime.parse(text, TIME_FORMATTER))
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
    
}
