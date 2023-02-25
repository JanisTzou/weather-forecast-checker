package com.google.weatherforecastchecker.scraper.measurement;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ChmuScraperTest {

    @Test
    void calculateScrapingTimes() {
        ChmuScraperProps properties = new ChmuScraperProps();
        properties.setScrapeEvery(Duration.ofHours(1));
        properties.setScrapeAtMinuteOfHour(30);

        List<LocalTime> scrapingTimes = properties.getScrapingTimes();

        assertEquals(24, scrapingTimes.size());
        for (int i = 0; i < scrapingTimes.size(); i++) {
            assertEquals(LocalTime.of(i, 30), scrapingTimes.get(i));
        }
    }

}
