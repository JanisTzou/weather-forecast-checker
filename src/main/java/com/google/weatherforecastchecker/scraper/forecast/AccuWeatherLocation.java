package com.google.weatherforecastchecker.scraper.forecast;

import com.google.weatherforecastchecker.Location;
import lombok.Data;

@Data
public class AccuWeatherLocation extends Location {
    private final String locationKey;

    public AccuWeatherLocation(String locationName, String latitude, String longitude, String locationKey) {
        super(locationName, latitude, longitude);
        this.locationKey = locationKey;
    }
}
