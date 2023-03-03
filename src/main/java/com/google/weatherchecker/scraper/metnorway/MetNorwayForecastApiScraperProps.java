package com.google.weatherchecker.scraper.metnorway;

import com.google.weatherchecker.scraper.ForecastScrapingProps;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("metnorway.api.forecast")
public class MetNorwayForecastApiScraperProps extends ForecastScrapingProps {
}
