package com.google.weatherchecker.scraper;

import com.google.weatherchecker.scraper.accuweather.AccuWeatherLocationApiScraper;
import com.google.weatherchecker.scraper.accuweather.AccuWeatherLocationProcessor;
import com.google.weatherchecker.scraper.chmu.ChmuMeasurementsScraper;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Log4j2
public class ScrapingManager {

    private final Schedulers schedulers;

    private final List<ForecastScraper<? extends LocationConfig>> forecastScrapers;
    private final ForecastProcessor forecastProcessor;

    private final AccuWeatherLocationApiScraper accuWeatherLocationApiScraper;
    private final AccuWeatherLocationProcessor accuWeatherLocationProcessor;

    private final ChmuMeasurementsScraper chmuMeasurementsScraper;
    private final CloudCoverageMeasurementsProcessor measurementsProcessor;

    public ScrapingManager(Schedulers schedulers,
                           List<ForecastScraper<? extends LocationConfig>> forecastScrapers,
                           ForecastProcessor forecastProcessor,
                           AccuWeatherLocationApiScraper accuWeatherLocationApiScraper,
                           AccuWeatherLocationProcessor accuWeatherLocationProcessor,
                           ChmuMeasurementsScraper chmuMeasurementsScraper,
                           CloudCoverageMeasurementsProcessor measurementsProcessor) {
        this.schedulers = schedulers;
        this.forecastScrapers = forecastScrapers;
        this.forecastProcessor = forecastProcessor;
        this.accuWeatherLocationApiScraper = accuWeatherLocationApiScraper;
        this.accuWeatherLocationProcessor = accuWeatherLocationProcessor;
        this.chmuMeasurementsScraper = chmuMeasurementsScraper;
        this.measurementsProcessor = measurementsProcessor;
    }

    public void startScraping() {
        log.info(">>> Starting scraping ...");
        for (ForecastScraper<? extends LocationConfig> scraper : forecastScrapers) {
            scraper.scheduleScrapingAllLocations(forecastProcessor::processForecast, schedulers);
        }
        accuWeatherLocationApiScraper.scheduleScrapingAllLocations(accuWeatherLocationProcessor::processLocationKey, schedulers);
        chmuMeasurementsScraper.startScraping(measurementsProcessor::processMeasurements, schedulers);
    }

}
