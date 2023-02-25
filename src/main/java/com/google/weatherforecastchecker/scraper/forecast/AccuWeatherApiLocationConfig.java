package com.google.weatherforecastchecker.scraper.forecast;

import com.google.weatherforecastchecker.scraper.LocationConfig;
import lombok.Getter;
import lombok.ToString;

@ToString
@Getter
public class AccuWeatherApiLocationConfig extends LocationConfig {

    private final String locationKey;

    public AccuWeatherApiLocationConfig(LocationConfig locationConfig, String locationKey) {
        super(locationConfig);
        this.locationKey = locationKey;
    }
}
