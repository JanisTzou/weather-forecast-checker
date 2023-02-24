package com.google.weatherforecastchecker.scraper.forecast;

import com.google.weatherforecastchecker.Utils;
import com.google.weatherforecastchecker.scraper.TimedScraping;
import lombok.Data;

import java.time.Duration;
import java.time.LocalTime;
import java.util.List;

@Data
abstract class ScrapingProperties implements TimedScraping {

    private String url;
    private boolean enabled;
    private int days;
    private String scrapingTimes;
    private Duration delayBetweenRequests;

    @Override
    public List<LocalTime> getScrapingTimes() {
        return Utils.parseScrapingTimes(scrapingTimes);
    }

}
