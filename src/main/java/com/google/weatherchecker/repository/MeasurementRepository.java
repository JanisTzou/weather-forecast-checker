package com.google.weatherchecker.repository;

import com.google.weatherchecker.model.CloudCoverageMeasurement;

public interface MeasurementRepository {

    void save(CloudCoverageMeasurement measurement);

}
