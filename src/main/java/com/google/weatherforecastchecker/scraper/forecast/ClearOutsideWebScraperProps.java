package com.google.weatherforecastchecker.scraper.forecast;

import com.google.weatherforecastchecker.scraper.ForecastScrapingProps;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("clearoutside.web.forecast")
public class ClearOutsideWebScraperProps extends ForecastScrapingProps {
}
