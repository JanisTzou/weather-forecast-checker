package com.google.weatherforecastchecker.scraper.measurement;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CloudCoverageMeasurement {

    private final LocalDateTime dateTime;
    private final String location;
    private final String description;
    private final Integer covered;

}
