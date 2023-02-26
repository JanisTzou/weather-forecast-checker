package com.google.weatherchecker.scraper.chmu;

import com.google.weatherchecker.model.Location;
import com.google.weatherchecker.scraper.LocationConfig;
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
