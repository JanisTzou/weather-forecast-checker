package com.google.weatherforecastchecker.scraper.measurement;

import com.google.weatherforecastchecker.Location;
import lombok.Data;

@Data
public class ChmuLocation extends Location {

    private final String stationName;

    public ChmuLocation(String locationName, String latitude, String longitude, String stationName) {
        super(locationName, latitude, longitude);
        this.stationName = stationName;
    }
}
