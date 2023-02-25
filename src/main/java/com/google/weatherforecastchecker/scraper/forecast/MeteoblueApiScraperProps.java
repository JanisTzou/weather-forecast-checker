package com.google.weatherforecastchecker.scraper.forecast;

import com.google.weatherforecastchecker.scraper.ForecastScrapingProps;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("meteoblue.api.forecast")
public class MeteoblueApiScraperProps extends ForecastScrapingProps {
}
