package com.google.weatherforecastchecker.scraper.measurement;

import com.google.weatherforecastchecker.Location;
import com.google.weatherforecastchecker.LocationConfig;
import lombok.Getter;

import java.util.Collections;


@Getter
public class ChmuLocationConfig extends LocationConfig {

    private final String stationName;

    public ChmuLocationConfig(Location location, String stationName) {
        super(location, true, Collections.emptyList());
        this.stationName = stationName;
    }
}
