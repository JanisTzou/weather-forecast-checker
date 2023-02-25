package com.google.weatherforecastchecker.scraper.forecast;

import com.google.weatherforecastchecker.scraper.LocationConfig;
import lombok.Getter;
import lombok.ToString;

@ToString
@Getter
public class AccuWeatherLocationConfig extends LocationConfig {

    private String locationKey;

    public AccuWeatherLocationConfig(LocationConfig locationConfig, String locationKey) {
        super(locationConfig);
        this.locationKey = locationKey;
    }
}
