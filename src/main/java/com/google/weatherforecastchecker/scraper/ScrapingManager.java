package com.google.weatherforecastchecker.scraper;

import com.google.weatherforecastchecker.scraper.forecast.ForecastScraper;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import java.util.List;

// TODO this is a "by locations" scraping manager ...
@Component
@Log4j2
public class ScrapingManager {

    private final Schedulers schedulers;
    private final List<ForecastScraper<? extends LocationConfig>> forecastScrapers;
    private final ForecastProcessor forecastProcessor;

    public ScrapingManager(Schedulers schedulers, List<ForecastScraper<? extends LocationConfig>> forecastScrapers,
                           ForecastProcessor forecastProcessor) {
        this.schedulers = schedulers;
        this.forecastScrapers = forecastScrapers;
        this.forecastProcessor = forecastProcessor;
    }

    public void startScrapingForecasts() {
        log.info(">>> Starting scraping ...");
        for (ForecastScraper<? extends LocationConfig> scraper : forecastScrapers) {
            scraper.startScrapingForcasts(forecastProcessor::processForecast, schedulers);
        }
    }

}
