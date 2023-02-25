package com.google.weatherforecastchecker.scraper.forecast;

import com.google.weatherforecastchecker.scraper.Location;
import lombok.Data;

@Data
public class AccuWeatherLocationKey {

    private final Location location;
    private final String locationKey;

}
