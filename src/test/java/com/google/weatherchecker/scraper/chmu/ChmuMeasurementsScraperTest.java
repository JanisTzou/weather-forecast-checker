package com.google.weatherchecker.scraper.chmu;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ChmuMeasurementsScraperTest {

    @Test
    void calculateScrapingTimes() {
        ChmuMeasurementsScraperProps properties = new ChmuMeasurementsScraperProps();
        properties.setScrapeEvery(Duration.ofHours(1));
        properties.setScrapeAtMinuteOfHour(30);

        List<LocalTime> scrapingTimes = properties.getScrapingTimes();

        assertEquals(24, scrapingTimes.size());
        for (int i = 0; i < scrapingTimes.size(); i++) {
            assertEquals(LocalTime.of(i, 30), scrapingTimes.get(i));
        }
    }

}
