package com.google.weatherforecastchecker.scraper.measurement;

import com.google.weatherforecastchecker.scraper.Location;
import com.google.weatherforecastchecker.scraper.Source;
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
