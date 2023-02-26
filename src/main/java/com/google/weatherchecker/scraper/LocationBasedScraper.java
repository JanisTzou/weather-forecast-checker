package com.google.weatherchecker.scraper;

import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface LocationBasedScraper<T extends LocationConfig, R> extends Scraper<LocationScrapingProps> {

    default void startScraping(Consumer<R> resultConsumer, Schedulers schedulers) {
        LocationScrapingProps properties = getScrapingProps();
        if (properties.isEnabled()) {
            if (properties.isScrapeOnceImmediately()) {
                List<T> locs = getEnabledLocationConfigs().collect(Collectors.toList());
                schedule(resultConsumer, schedulers, properties, null, locs, true);
            }
            Map<LocalTime, List<T>> locationsByTime = getEnabledLocationConfigs()
                    .flatMap(loc -> getScrapingTimes(properties, loc).stream().map(time -> new TimedLocationConfig<>(time, loc)))
                    .collect(Collectors.groupingBy(TimedLocationConfig::getScrapingTime,
                            Collectors.mapping(TimedLocationConfig::getLocationConfig, Collectors.toList())));

            locationsByTime.forEach((scrapingTime, locs) -> {
                schedule(resultConsumer, schedulers, properties, scrapingTime, locs, false);
            });
        }
    }

    private Stream<T> getEnabledLocationConfigs() {
        return getLocationConfigs().stream().filter(LocationConfig::isEnabled);
    }

    private void schedule(Consumer<R> resultConsumer, Schedulers schedulers, LocationScrapingProps properties, LocalTime scrapingTime, List<T> locs, boolean scrapeOnceImmediately) {
        Scheduler scheduler = schedulers.getScheduler(getSource());
        Function<T, Callable<Optional<R>>> scrapingTask = loc -> () -> scrape(loc);
        ScrapingByLocation<T, R> scraping = new ScrapingByLocation<>(scrapingTask, resultConsumer, scrapingTime, locs, getSource(), properties, scrapeOnceImmediately);
        scheduler.schedule(scraping);
    }

    Optional<R> scrape(T locationConfig);

    List<T> getLocationConfigs();

    private List<LocalTime> getScrapingTimes(ScrapingProps properties, T location) {
        List<LocalTime> defaultTimes = properties.getScrapingTimes();
        List<LocalTime> locationTimes = location.getScrapingTimes();
        return locationTimes.isEmpty() ? defaultTimes : locationTimes;
    }

}
