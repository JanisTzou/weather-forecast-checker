package com.google.weatherchecker.scraper;

import com.google.weatherchecker.model.Source;
import jakarta.annotation.Nullable;
import lombok.Data;

import java.time.LocalTime;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

/**
 * @param <R> result of the scraping
 */
@Data
public class ScrapingByAnything<R> {

    private final Callable<Optional<R>> scraping;
    private final Consumer<R> resultConsumer;

    @Nullable
    private final LocalTime scrapingTime;
    private final Source source;
    private final ScrapingProps scrapingProps;
    private final boolean scrapeOnceImmediately;

}
