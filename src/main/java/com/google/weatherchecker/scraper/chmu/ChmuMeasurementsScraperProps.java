package com.google.weatherchecker.scraper.chmu;

import com.google.weatherchecker.scraper.ScrapingProps;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Data
@ConfigurationProperties("chmu.web.measurement")
public class ChmuMeasurementsScraperProps extends ScrapingProps {

    private Duration scrapeEvery;
    private int scrapeAtMinuteOfHour;

    @Override
    public List<LocalTime> getScrapingTimes() {
        long timesPerDay = Duration.ofDays(1).dividedBy(scrapeEvery);
        List<LocalTime> scrapingTimes = new ArrayList<>();
        LocalTime beginning = LocalTime.MIDNIGHT.plusMinutes(scrapeAtMinuteOfHour);
        for (long i = 0; i < timesPerDay; i++) {
            scrapingTimes.add(beginning.plus(scrapeEvery.multipliedBy(i)));
        }
        return scrapingTimes;
    }

}
