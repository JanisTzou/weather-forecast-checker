package com.google.weatherchecker.model;

import lombok.Data;

import java.util.List;

@Data
public class CloudCoverageMeasurements {

    private final List<CloudCoverageMeasurement> measurements;

}
