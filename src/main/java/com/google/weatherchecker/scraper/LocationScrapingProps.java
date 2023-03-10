package com.google.weatherchecker.scraper;

import com.google.weatherchecker.util.Utils;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public abstract class LocationScrapingProps extends ScrapingProps {

    private String scrapingTimes;

    @Override
    public List<LocalTime> getScrapingTimes() {
        return Utils.parseCommaSeparatedTimes(scrapingTimes);
    }


}
