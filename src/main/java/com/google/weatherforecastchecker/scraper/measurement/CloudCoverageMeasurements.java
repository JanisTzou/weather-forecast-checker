package com.google.weatherforecastchecker.scraper.measurement;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class CloudCoverageMeasurements {

    private final List<CloudCoverageMeasurement> measurements;

}
