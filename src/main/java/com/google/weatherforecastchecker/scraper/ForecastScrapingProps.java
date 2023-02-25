package com.google.weatherforecastchecker.scraper;

import com.google.weatherforecastchecker.util.Utils;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Duration;
import java.time.LocalTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public abstract class ForecastScrapingProps extends LocationScrapingProps {

    private int days;
    private String scrapingTimes;
    private Duration delayBetweenLocations;

    @Override
    public List<LocalTime> getScrapingTimes() {
        return Utils.parseCommaSeparatedTimes(scrapingTimes);
    }

}
