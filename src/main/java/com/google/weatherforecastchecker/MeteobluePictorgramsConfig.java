package com.google.weatherforecastchecker;

import lombok.Data;

@Data
public class MeteobluePictorgramsConfig {
    private final int pictogramId;
    private final String description;
    private final int cloudCoverage;
}