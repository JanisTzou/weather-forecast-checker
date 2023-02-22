package com.google.weatherforecastchecker;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Location {
    private String locationName;
    private String latitude;
    private String longitude;
}
