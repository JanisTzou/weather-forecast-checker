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
public abstract class LocationScrapingProps extends ScrapingProps {

    private Duration delayBetweenLocations;
    private String scrapingTimes;

    @Override
    public List<LocalTime> getScrapingTimes() {
        return Utils.parseCommaSeparatedTimes(scrapingTimes);
    }


}
