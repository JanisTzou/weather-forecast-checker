package com.google.weatherforecastchecker.scraper.forecast;

import com.google.weatherforecastchecker.LocationConfig;
import com.google.weatherforecastchecker.util.Utils;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public interface ForecastScraper<T extends LocationConfig> {

    default List<Forecast> scrape(List<T> locationConfigs) {
        return locationConfigs.stream()
                .filter(c -> getScrapingProperties().isEnabled())
                .filter(LocationConfig::isEnabled)
                .flatMap(l -> scrape(l).stream())
                .peek(f -> {
                    System.out.println(f);
                    Utils.sleep(getScrapingProperties().getDelayBetweenRequests().toMillis());
                })
                .collect(Collectors.toList());
    }


    Optional<Forecast> scrape(T location);

    Source getSource();

    ScrapingProperties getScrapingProperties();

}
