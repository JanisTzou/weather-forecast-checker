package com.google.weatherchecker.scraper.accuweather;

import com.google.weatherchecker.scraper.LocationConfig;
import lombok.Getter;
import lombok.ToString;

@ToString
@Getter
public class AccuWeatherLocationApiConfig extends LocationConfig {

    private final String locationKey;

    public AccuWeatherLocationApiConfig(LocationConfig locationConfig, String locationKey) {
        super(locationConfig);
        this.locationKey = locationKey;
    }
}
