package com.google.weatherchecker.scraper.accuweather;

import com.google.weatherchecker.scraper.ForecastScrapingProps;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("accuweather.api.forecast")
public class AccuWeatherForecastApiScraperProps extends ForecastScrapingProps {
}
