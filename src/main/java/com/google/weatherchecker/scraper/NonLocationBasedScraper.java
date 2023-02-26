package com.google.weatherchecker.scraper;

import java.time.LocalTime;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

public interface NonLocationBasedScraper<R> extends Scraper<ScrapingProps> {

    default void startScraping(Consumer<R> resultConsumer, Schedulers schedulers) {
        ScrapingProps properties = getScrapingProps();
        if (properties.isEnabled()) {
            if (properties.isScrapeOnceImmediately()) {
                schedule(resultConsumer, schedulers, properties, null, true);
            }
            for (LocalTime scrapingTime : properties.getScrapingTimes()) {
                schedule(resultConsumer, schedulers, properties, scrapingTime, false);
            }
        }
    }

    private void schedule(Consumer<R> resultConsumer, Schedulers schedulers, ScrapingProps properties, LocalTime scrapingTime, boolean scrapeOnceImmediately) {
        Scheduler scheduler = schedulers.getScheduler(getSource());
        Callable<Optional<R>> scrapingTask = this::scrape;
        ScrapingByAnything<R> scraping = new ScrapingByAnything<>(scrapingTask, resultConsumer, scrapingTime, getSource(), properties, scrapeOnceImmediately);
        scheduler.schedule(scraping);
    }

    Optional<R> scrape();

}
