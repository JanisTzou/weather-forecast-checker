package com.google.weatherforecastchecker.scraper;

import com.google.weatherforecastchecker.util.DateTimeUtils;
import com.google.weatherforecastchecker.util.Utils;
import lombok.extern.log4j.Log4j2;

import java.time.Duration;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Log4j2
public class Scheduler {

    private static final Duration oneDay = Duration.ofDays(1L);
    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    public <T extends LocationConfig, R> void schedule(ScrapingByLocation<T, R> scraping) {
        Duration initialDelay = DateTimeUtils.untilNextTime(LocalTime.now(), scraping.getScrapingTime(), ChronoUnit.SECONDS);
        log.info("Scheduling scraping of {} locations with initial delay {} from source {}", scraping.getLocations().size(), initialDelay, scraping.getSource());
        ScrapeLocations<T, R> scrapeLocations = new ScrapeLocations<>(scraping);
        executor.scheduleAtFixedRate(scrapeLocations, initialDelay.toMillis(), oneDay.toMillis(), TimeUnit.MILLISECONDS);
    }

    private static class ScrapeLocations<T extends LocationConfig, R> implements Runnable {

        private final List<T> locations = new ArrayList<>();
        private final Queue<T> runQueue = new LinkedList<>();
        private final ScrapingByLocation<T, R> scraping;

        public ScrapeLocations(ScrapingByLocation<T, R> scraping) {
            locations.addAll(scraping.getLocations());
            this.scraping = scraping;
        }

        @Override
        public void run() {
            runQueue.addAll(locations);
            for (T location : runQueue) {
                // TODO remove from queue ...
                // TODO somehow we need to know if there was a problem or not ... if not handle the result, if yes then repeat ...
                try {
//                    Optional<R> result = scraping.getScraping().apply(location).call(); // TODO uncomment again ...
                    Optional<R> result = Optional.empty();
                    if (result.isPresent()) {
                        scraping.getResultConsumer().accept(result.get());
                    } else {
                        // TODO...
                    }
                    Utils.sleep(scraping.getScrapingProps().getDelayBetweenLocations().toMillis());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                // TODO apply delay ...
                // TODO add back to queue if there is an error ... N times ...
            }
        }
    }

}
