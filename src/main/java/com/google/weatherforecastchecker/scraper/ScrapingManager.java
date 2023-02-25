package com.google.weatherforecastchecker.scraper;

import com.google.weatherforecastchecker.scraper.forecast.ForecastScraper;
import com.google.weatherforecastchecker.scraper.forecast.LocationBasedScraper;
import lombok.Data;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

// TODO this is a "by locations" scraping manager ...
@Component
@Log4j2
public class ScrapingManager {

    private final Map<Source, Scheduler> schedulers = new HashMap<>();

    private final List<ForecastScraper<? extends LocationConfig>> forecastScrapers;

    private final ForecastProcessor forecastProcessor;

    public ScrapingManager(List<ForecastScraper<? extends LocationConfig>> forecastScrapers,
                           ForecastProcessor forecastProcessor) {
        this.forecastScrapers = forecastScrapers;
        this.forecastProcessor = forecastProcessor;
    }

    public void startScrapingForecasts() {
        log.info(">>> Starting scraping for {} scraper", forecastScrapers.size());
        for (ForecastScraper<?> scraper : forecastScrapers) {
            startScrapingLocations(scraper, forecastProcessor::processForecast, LocationConfigRepository.getLocationConfigs(scraper.getSource()));
        }
    }

    public <T extends LocationConfig, R> void startScrapingLocations(LocationBasedScraper<T, R> scraper, Consumer<R> resultConsumer, List<T> locations) {
        ForecastScrapingProps properties = scraper.getScrapingProps();
        if (properties.isEnabled()) {
            Map<LocalTime, List<T>> locationsByTime = locations.stream()
                    .filter(LocationConfig::isEnabled)
                    .flatMap(loc -> getScrapingTimes(properties, loc).stream().map(time -> new TimedLocationConfig<>(time, loc)))
                    .collect(Collectors.groupingBy(TimedLocationConfig::getScrapingTime,
                            Collectors.mapping(TimedLocationConfig::getLocationConfig, Collectors.toList())));

            locationsByTime.forEach((scrapingTime, locs) -> {
                Scheduler scheduler = schedulers.computeIfAbsent(scraper.getSource(), s -> new Scheduler());
                Function<T, Callable<Optional<R>>> scrapingTask = loc -> () -> scraper.scrape(loc);
                ScrapingByLocation<T, R> scraping = new ScrapingByLocation<>(scrapingTask, resultConsumer, scrapingTime, locs, scraper.getSource(), properties);
                scheduler.schedule(scraping);
            });
        }
    }

    private <T extends LocationConfig> List<LocalTime> getScrapingTimes(ScrapingProps properties, T location) {
        List<LocalTime> defaultTimes = properties.getScrapingTimes();
        List<LocalTime> locationTimes = location.getScrapingTimes();
        return locationTimes.isEmpty() ? defaultTimes : locationTimes;
    }


    @Data
    private static class TimedLocationConfig<T extends LocationConfig> {
        private final LocalTime scrapingTime;
        private final T locationConfig;
    }


}
