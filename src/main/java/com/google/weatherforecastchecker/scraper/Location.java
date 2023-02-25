package com.google.weatherforecastchecker.scraper;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@RequiredArgsConstructor
@Getter
@ToString
public class Location {

    private final String name;
    private final String latitude;
    private final String longitude;

}