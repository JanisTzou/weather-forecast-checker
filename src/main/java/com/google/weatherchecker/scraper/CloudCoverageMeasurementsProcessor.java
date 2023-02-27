package com.google.weatherchecker.scraper;

import com.google.weatherchecker.repository.MeasurementRepository;
import com.google.weatherchecker.repository.SerialDatabaseWriter;
import com.google.weatherchecker.model.CloudCoverageMeasurement;
import com.google.weatherchecker.model.CloudCoverageMeasurements;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

@Log4j2
@Component
@RequiredArgsConstructor
public class CloudCoverageMeasurementsProcessor {

    private final MeasurementRepository measurementRepository;
    private final SerialDatabaseWriter serialDatabaseWriter;
    private final LocationEnricher locationEnricher;

    public void processMeasurements(CloudCoverageMeasurements measurements) {
        log.info("Received measurements: {}", measurements);
        for (CloudCoverageMeasurement measurement : measurements.getMeasurements()) {
            locationEnricher.enrich(measurement.getLocation());
            serialDatabaseWriter.execute(() -> measurementRepository.save(measurement));
        }
    }

}
