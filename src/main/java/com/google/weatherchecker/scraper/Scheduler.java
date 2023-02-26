package com.google.weatherchecker.scraper;

import com.google.weatherchecker.model.Source;
import com.google.weatherchecker.util.DateTimeUtils;
import com.google.weatherchecker.util.Utils;
import lombok.Data;
import lombok.extern.log4j.Log4j2;

import java.time.Duration;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Log4j2
public class Scheduler {

    private static final Duration oneDay = Duration.ofDays(1L);
    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    public <R> void schedule(ScrapingByAnything<R> scraping) {
        if (scraping.isScrapeOnceImmediately()) {
            log.info("Immediately scraping from source {}", scraping.getSource());
            ScrapeNonLocations<R> scrapeLocations = new ScrapeNonLocations<>(scraping);
            executor.schedule(scrapeLocations, 0, TimeUnit.SECONDS);
        } else {
            Duration initialDelay = DateTimeUtils.untilNextTime(LocalTime.now(), scraping.getScrapingTime(), ChronoUnit.SECONDS);
            log.info("Scheduling scraping with initial delay {} from source {}", initialDelay, scraping.getSource());
            ScrapeNonLocations<R> scrapeLocations = new ScrapeNonLocations<>(scraping);
            executor.scheduleAtFixedRate(scrapeLocations, initialDelay.toMillis(), oneDay.toMillis(), TimeUnit.MILLISECONDS);
        }
    }

    public <T extends LocationConfig, R> void schedule(ScrapingByLocation<T, R> scraping) {
        if (scraping.isScrapeOnceImmediately()) {
            log.info("Immediately scraping {} locations from source {}", scraping.getLocations().size(), scraping.getSource());
            ScrapeLocations<T, R> scrapeLocations = new ScrapeLocations<>(scraping);
            executor.schedule(scrapeLocations, 0, TimeUnit.SECONDS);
        } else {
            Duration initialDelay = DateTimeUtils.untilNextTime(LocalTime.now(), scraping.getScrapingTime(), ChronoUnit.SECONDS);
            log.info("Scheduling scraping of {} locations with initial delay {} from source {}", scraping.getLocations().size(), initialDelay, scraping.getSource());
            ScrapeLocations<T, R> scrapeLocations = new ScrapeLocations<>(scraping);
            executor.scheduleAtFixedRate(scrapeLocations, initialDelay.toMillis(), oneDay.toMillis(), TimeUnit.MILLISECONDS);
        }
    }

    @Data
    abstract static class Scrape<T, R> implements Runnable {
        protected final Source source;
        protected final Consumer<R> resultConsumer;
        protected final Duration delayBetweenRequests;

        protected List<ScrapingTask<T>> scrapingTasks;
        protected final Queue<ScrapingTask<T>> taskQueue = new LinkedList<>();

        protected abstract Optional<R> scrape(ScrapingTask<T> next) throws Exception;

        @Override
        public void run() {
            try {
                taskQueue.addAll(scrapingTasks);
                while (!taskQueue.isEmpty()) {
                    ScrapingTask<T> next = taskQueue.poll();
                    next.attemptCount.incrementAndGet();
                    try {
                        Optional<R> result = scrape(next);
                        if (result.isPresent()) {
                            resultConsumer.accept(result.get());
                        } else {
                            boolean retry = enqueueAgain(next);
                            if (retry) {
                                log.warn("Failed to scrape data from {}, will try again ", source);
                            } else {
                                log.warn("Failed to scrape data from {}, will not try any more ...", source);
                            }
                        }
                    } catch (Exception e) {
                        boolean retry = enqueueAgain(next);
                        String retryMsg = retry ? ", will try again" : "";
                        log.error("Error while scraping from source {} {}", source, retryMsg, e); // TODO provide some description ?
                    }
                    Utils.sleep(delayBetweenRequests.toMillis());
                }
            } catch (Exception e) {
                log.error("Error running scraping task!", e);
            }
        }

        private boolean enqueueAgain(ScrapingTask<T> next) {
            if (next.attemptCount.get() < 3) {
                taskQueue.add(next);
                return true;
            }
            return false;
        }
    }


    private static class ScrapeNonLocations<R> extends Scrape<Void, R> {
        private final ScrapingByAnything<R> scraping;

        private ScrapeNonLocations(ScrapingByAnything<R> scraping) {
            super(scraping.getSource(), scraping.getResultConsumer(), scraping.getScrapingProps().getDelayBetweenRequests());
            this.scrapingTasks = List.of(new ScrapingTask<>(null));
            this.scraping = scraping;
        }

        @Override
        protected Optional<R> scrape(ScrapingTask<Void> next) throws Exception {
            log.info("Scraping from {}", source);
            return scraping.getScraping().call();
        }
    }


    private static class ScrapeLocations<T extends LocationConfig, R> extends Scrape<T, R> {
        private final ScrapingByLocation<T, R> scraping;

        public ScrapeLocations(ScrapingByLocation<T, R> scraping) {
            super(scraping.getSource(), scraping.getResultConsumer(), scraping.getScrapingProps().getDelayBetweenRequests());
            this.scrapingTasks = scraping.getLocations().stream().map(ScrapingTask::new).collect(Collectors.toList());
            this.scraping = scraping;
        }

        protected Optional<R> scrape(ScrapingTask<T> next) throws Exception {
            log.info("Scraping {} from {}", next.item.getName(), source);
            return scraping.getScraping().apply(next.item).call();
        }
    }

    @Data
    private static class ScrapingTask<T> {
        private final AtomicInteger attemptCount = new AtomicInteger(0);
        private final T item;
    }

}
