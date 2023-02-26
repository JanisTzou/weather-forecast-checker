package com.google.weatherchecker.scraper.aladin;

import com.google.weatherchecker.scraper.ForecastScrapingProps;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("aladin.api.forecast")
public class AladinForecastApiScraperProps extends ForecastScrapingProps {
}
