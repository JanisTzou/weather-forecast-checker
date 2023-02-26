package com.google.weatherchecker.scraper.meteoblue;

import com.google.weatherchecker.scraper.ForecastScrapingProps;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("meteoblue.web.forecast")
public class MeteoblueForecastWebScraperProps extends ForecastScrapingProps {
}
