package com.google.weatherforecastchecker;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Location {

    private String locationName;
    private String latitude;
    private String longitude;

    public Location(String locationName, String latitude, String longitude) {
        this.locationName = locationName;
        this.latitude = latitude;
        this.longitude = longitude;
    }
}
