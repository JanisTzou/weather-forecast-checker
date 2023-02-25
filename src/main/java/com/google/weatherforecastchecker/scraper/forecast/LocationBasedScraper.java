package com.google.weatherforecastchecker.scraper.forecast;

import com.google.weatherforecastchecker.scraper.ForecastScrapingProps;
import com.google.weatherforecastchecker.scraper.LocationConfig;
import com.google.weatherforecastchecker.scraper.Scraper;

import java.util.Optional;

public interface LocationBasedScraper<T extends LocationConfig, R> extends Scraper<ForecastScrapingProps> {

    Optional<R> scrape(T locationConfig);

}
