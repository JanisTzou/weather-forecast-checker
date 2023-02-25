package com.google.weatherforecastchecker.scraper.forecast;

import com.google.weatherforecastchecker.scraper.ForecastScrapingProps;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("accuweather.api.forecast")
public class AccuWeatherApiScraperProps extends ForecastScrapingProps {
}
