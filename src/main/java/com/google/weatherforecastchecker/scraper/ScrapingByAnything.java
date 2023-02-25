package com.google.weatherforecastchecker.scraper;

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
public class ScrapingByAnything<R> {

    private final Callable<Optional<R>> scraping;
    private final Consumer<R> resultConsumer;

    @Nullable
    private final LocalTime scrapingTime;
    private final Source source;
    private final ScrapingProps scrapingProps;

}
