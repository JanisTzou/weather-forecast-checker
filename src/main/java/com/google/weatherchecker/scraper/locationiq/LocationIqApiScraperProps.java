package com.google.weatherchecker.scraper.locationiq;

import com.google.weatherchecker.scraper.ForecastScrapingProps;
import com.google.weatherchecker.scraper.LocationScrapingProps;
import com.google.weatherchecker.scraper.ScrapingProps;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("locationiq.api.reverse.geocoding")
public class LocationIqApiScraperProps extends LocationScrapingProps {
}
