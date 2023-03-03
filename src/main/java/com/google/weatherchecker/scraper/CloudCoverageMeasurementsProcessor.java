package com.google.weatherchecker.scraper;

import com.google.weatherchecker.model.CloudCoverageMeasurement;
import com.google.weatherchecker.model.CloudCoverageMeasurements;
import com.google.weatherchecker.repository.MeasurementRepository;
import com.google.weatherchecker.repository.SerialDatabaseWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Log4j2
@Component
@RequiredArgsConstructor
public class CloudCoverageMeasurementsProcessor {

    private final MeasurementRepository measurementRepository;
    private final SerialDatabaseWriter serialDatabaseWriter;
    private final LocationEnricher locationEnricher;
    private final List<MeasurementsScrapedListener> measurementsScrapedListeners = new ArrayList<>();

    @Autowired
    public void addListener(List<MeasurementsScrapedListener> listener) {
        this.measurementsScrapedListeners.addAll(listener);
    }

    public void processMeasurements(CloudCoverageMeasurements measurements) {
        log.info("Received measurements: {}", measurements);
        for (CloudCoverageMeasurement measurement : measurements.getMeasurements()) {
            locationEnricher.enrich(measurement.getLocation());
            serialDatabaseWriter.execute(() -> measurementRepository.save(measurement));
        }
        notifyListeners();
    }

    private void notifyListeners() {
        for (MeasurementsScrapedListener listener : measurementsScrapedListeners) {
            listener.onAllLocationsScraped();
        }
    }

}
