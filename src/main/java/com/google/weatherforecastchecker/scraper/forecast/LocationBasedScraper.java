package com.google.weatherforecastchecker.scraper.forecast;

import com.google.weatherforecastchecker.scraper.*;

import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public interface LocationBasedScraper<T extends LocationConfig, R> extends Scraper<ForecastScrapingProps> {

    default void startScrapingForcasts(Consumer<R> resultConsumer, Schedulers schedulers) {
        ForecastScrapingProps properties = getScrapingProps();
        if (properties.isEnabled()) {
            Map<LocalTime, List<T>> locationsByTime = getLocationConfigs().stream()
                    .filter(LocationConfig::isEnabled)
                    .flatMap(loc -> getScrapingTimes(properties, loc).stream().map(time -> new TimedLocationConfig<>(time, loc)))
                    .collect(Collectors.groupingBy(TimedLocationConfig::getScrapingTime,
                            Collectors.mapping(TimedLocationConfig::getLocationConfig, Collectors.toList())));

            locationsByTime.forEach((scrapingTime, locs) -> {
                Scheduler scheduler = schedulers.getScheduler(getSource());
                Function<T, Callable<Optional<R>>> scrapingTask = loc -> () -> scrape(loc);
                ScrapingByLocation<T, R> scraping = new ScrapingByLocation<>(scrapingTask, resultConsumer, scrapingTime, locs, getSource(), properties);
                scheduler.schedule(scraping);
            });
        }
    }

    Optional<R> scrape(T locationConfig);

    List<T> getLocationConfigs();

    private List<LocalTime> getScrapingTimes(ScrapingProps properties, T location) {
        List<LocalTime> defaultTimes = properties.getScrapingTimes();
        List<LocalTime> locationTimes = location.getScrapingTimes();
        return locationTimes.isEmpty() ? defaultTimes : locationTimes;
    }

}
