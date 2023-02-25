package com.google.weatherforecastchecker.scraper.forecast;

import com.google.weatherforecastchecker.scraper.LocationConfig;

public interface ForecastScraper<T extends LocationConfig> extends LocationBasedScraper<T, Forecast> {
}
