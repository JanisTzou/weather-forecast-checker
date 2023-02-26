package com.google.weatherforecastchecker.repos;

import com.google.weatherforecastchecker.scraper.measurement.CloudCoverageMeasurement;

public interface MeasurementRepository {

    void save(CloudCoverageMeasurement measurement);

}
