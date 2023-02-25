package com.google.weatherforecastchecker.scraper.measurement;

import com.google.weatherforecastchecker.scraper.Location;
import com.google.weatherforecastchecker.scraper.LocationConfig;
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
