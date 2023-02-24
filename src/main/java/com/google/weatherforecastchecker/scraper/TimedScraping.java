package com.google.weatherforecastchecker.scraper;

import java.time.LocalTime;
import java.util.List;

public interface TimedScraping {

    List<LocalTime> getScrapingTimes();

}
