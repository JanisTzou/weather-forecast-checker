package com.google.weatherforecastchecker.scraper.forcast;

import com.google.weatherforecastchecker.Location;

import java.util.List;
import java.util.Optional;

public interface ForecastScraper<T extends Location> {

    List<Forecast> scrape(List<T> locations);

    Optional<Forecast> scrape(T location);

}
