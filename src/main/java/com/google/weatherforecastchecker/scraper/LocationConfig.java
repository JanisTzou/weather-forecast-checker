package com.google.weatherforecastchecker.scraper;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.time.LocalTime;
import java.util.List;

@Getter
@RequiredArgsConstructor
@ToString
public class LocationConfig {

    private final Location location;
    private final boolean enabled;
    private final List<LocalTime> scrapingTimes;

    public LocationConfig(LocationConfig other) {
        this.location = other.getLocation();
        this.enabled = other.isEnabled();
        this.scrapingTimes = other.getScrapingTimes();
    }

    public String getName() {
        return location.getName();
    }

    public double getLatitude() {
        return location.getLatitude();
    }

    public double getLongitude() {
        return location.getLongitude();
    }

}
