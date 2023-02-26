package com.google.weatherchecker.scraper.accuweather;

import com.google.weatherchecker.scraper.LocationScrapingProps;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("accuweather.api.locations")
public class AccuWeatherLocationApiScraperProps extends LocationScrapingProps {
}
