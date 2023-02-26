package com.google.weatherchecker.scraper;

import com.google.weatherchecker.model.Forecast;

public interface ForecastScraper<T extends LocationConfig> extends LocationBasedScraper<T, Forecast> {
}
