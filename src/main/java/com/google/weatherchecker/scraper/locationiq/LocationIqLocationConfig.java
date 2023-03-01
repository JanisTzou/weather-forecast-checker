package com.google.weatherchecker.scraper.locationiq;

import com.google.weatherchecker.scraper.LocationConfig;
import lombok.Getter;
import lombok.ToString;

import java.util.Optional;

@ToString
@Getter
public class LocationIqLocationConfig extends LocationConfig {

    private final Optional<String> countyNameOverride;
    private final Optional<String> regionNameOverride;

    public LocationIqLocationConfig(LocationConfig locationConfig, String countyNameOverride, String regionNameOverride) {
        super(locationConfig);
        this.countyNameOverride = Optional.ofNullable(countyNameOverride);
        this.regionNameOverride = Optional.ofNullable(regionNameOverride);
    }
}
