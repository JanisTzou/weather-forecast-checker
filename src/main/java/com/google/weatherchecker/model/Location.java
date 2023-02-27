package com.google.weatherchecker.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@RequiredArgsConstructor
@Getter
@ToString
public class Location {

    private final String name;
    private final double latitude;
    private final double longitude;
    // okres
    private final String municipality;
    // kraj
    private final String county;
    private final String region;
    private final boolean complete;

    public Location(String name, double latitude, double longitude) {
        this(name, latitude, longitude, null, null, null, false);
    }

}
