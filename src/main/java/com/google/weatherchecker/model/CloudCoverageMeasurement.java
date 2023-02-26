package com.google.weatherchecker.model;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CloudCoverageMeasurement {

    private final LocalDateTime scraped;
    private final LocalDateTime dateTime;
    private final Location location;
    private final String description;
    private final Integer cloudCoverageTotal;
    private final Source source;

}
