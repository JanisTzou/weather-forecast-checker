package com.google.weatherchecker.scraper.clearoutside;

import com.google.weatherchecker.scraper.ForecastScrapingProps;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("clearoutside.web.forecast")
public class ClearOutsideForecastWebScraperProps extends ForecastScrapingProps {
}
