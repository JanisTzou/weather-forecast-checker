package com.google.weatherchecker.scraper.meteoblue;

import lombok.Data;

@Data
public class MeteobluePictorgramsConfig {
    private final int pictogramId;
    private final String description;
    private final int cloudCoverage;
}
