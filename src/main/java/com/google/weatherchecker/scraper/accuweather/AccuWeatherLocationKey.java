package com.google.weatherchecker.scraper.accuweather;

import com.google.weatherchecker.model.Location;
import lombok.Data;

@Data
public class AccuWeatherLocationKey {

    private final Location location;
    private final String locationKey;

}
