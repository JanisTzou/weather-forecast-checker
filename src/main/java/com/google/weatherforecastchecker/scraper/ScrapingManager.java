package com.google.weatherforecastchecker.scraper;

import com.google.weatherforecastchecker.scraper.forecast.AccuWeatherApiLocationScraper;
import com.google.weatherforecastchecker.scraper.forecast.AccuWeatherLocationProcessor;
import com.google.weatherforecastchecker.scraper.forecast.ForecastScraper;
import com.google.weatherforecastchecker.scraper.measurement.ChmuMeasurementsScraper;
import com.google.weatherforecastchecker.scraper.measurement.CloudCoverageMeasurementsProcessor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Log4j2
public class ScrapingManager {

    private final Schedulers schedulers;

    private final List<ForecastScraper<? extends LocationConfig>> forecastScrapers;
    private final ForecastProcessor forecastProcessor;

    private final AccuWeatherApiLocationScraper accuWeatherApiLocationScraper;
    private final AccuWeatherLocationProcessor accuWeatherLocationProcessor;

    private final ChmuMeasurementsScraper chmuMeasurementsScraper;
    private final CloudCoverageMeasurementsProcessor measurementsProcessor;

    public ScrapingManager(Schedulers schedulers,
                           List<ForecastScraper<? extends LocationConfig>> forecastScrapers,
                           ForecastProcessor forecastProcessor,
                           AccuWeatherApiLocationScraper accuWeatherApiLocationScraper,
                           AccuWeatherLocationProcessor accuWeatherLocationProcessor,
                           ChmuMeasurementsScraper chmuMeasurementsScraper,
                           CloudCoverageMeasurementsProcessor measurementsProcessor) {
        this.schedulers = schedulers;
        this.forecastScrapers = forecastScrapers;
        this.forecastProcessor = forecastProcessor;
        this.accuWeatherApiLocationScraper = accuWeatherApiLocationScraper;
        this.accuWeatherLocationProcessor = accuWeatherLocationProcessor;
        this.chmuMeasurementsScraper = chmuMeasurementsScraper;
        this.measurementsProcessor = measurementsProcessor;
    }

    public void startScrapingForecasts() {
        log.info(">>> Starting scraping ...");
        for (ForecastScraper<? extends LocationConfig> scraper : forecastScrapers) {
            scraper.startScraping(forecastProcessor::processForecast, schedulers);
        }
        accuWeatherApiLocationScraper.startScraping(accuWeatherLocationProcessor::processLocationKey, schedulers);
        chmuMeasurementsScraper.startScraping(measurementsProcessor::processMeasurements, schedulers);
    }

}
