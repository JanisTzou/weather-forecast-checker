package com.google.weatherforecastchecker.scraper.forcast;

import com.google.weatherforecastchecker.Location;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AccuWeatherLocation extends Location {
    private final String locationKey;
}
