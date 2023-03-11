package com.google.weatherchecker.scraper.openmeteo.ecmwf;

import com.google.weatherchecker.scraper.ForecastScrapingProps;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("open-meteo.ecmwf.api.forecast")
public class OpenMeteoEcmwfForecastApiScraperProps extends ForecastScrapingProps {
}
