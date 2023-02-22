package com.google.weatherforecastchecker.scraper.forcast;

import com.google.weatherforecastchecker.Location;

import java.util.List;

public interface ForecastScraper<T extends Location> {

    List<DayForecast> scrape(List<T> locations);

    List<DayForecast> scrape(T location);

}
