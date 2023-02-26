package com.google.weatherchecker.scraper;

import com.google.weatherchecker.model.Source;
import jakarta.annotation.Nullable;
import lombok.Data;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @param <R> result of the scraping
 */
@Data
public class ScrapingByLocation<T extends LocationConfig, R> {

    private final Function<T, Callable<Optional<R>>> scraping;
    private final Consumer<R> resultConsumer;

    @Nullable
    private final LocalTime scrapingTime;
    private final List<T> locations;
    private final Source source;
    private final LocationScrapingProps scrapingProps;
    private final boolean scrapeOnceImmediately;

}
