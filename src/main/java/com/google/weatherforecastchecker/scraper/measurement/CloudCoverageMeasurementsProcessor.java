package com.google.weatherforecastchecker.scraper.measurement;

import com.google.weatherforecastchecker.repos.MeasurementRepository;
import com.google.weatherforecastchecker.repos.SerialDatabaseWriter;
import com.google.weatherforecastchecker.scraper.forecast.AccuWeatherLocationKey;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

@Log4j2
@Component
@RequiredArgsConstructor
public class CloudCoverageMeasurementsProcessor {

    private final MeasurementRepository measurementRepository;
    private final SerialDatabaseWriter serialDatabaseWriter;

    public void processMeasurements(CloudCoverageMeasurements measurements) {
        log.info("Received measurements: {}", measurements);
        for (CloudCoverageMeasurement measurement : measurements.getMeasurements()) {
            serialDatabaseWriter.execute(() -> measurementRepository.save(measurement));
        }
    }

}
