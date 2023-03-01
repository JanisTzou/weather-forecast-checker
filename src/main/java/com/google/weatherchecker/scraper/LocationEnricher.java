package com.google.weatherchecker.scraper;

import com.google.weatherchecker.model.Location;
import com.google.weatherchecker.model.Source;
import com.google.weatherchecker.repository.LocationRepository;
import com.google.weatherchecker.repository.SerialDatabaseWriter;
import com.google.weatherchecker.scraper.locationiq.LocationIqApiScraper;
import com.google.weatherchecker.scraper.locationiq.LocationIqLocationConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.function.Consumer;

@Log4j2
@Component
@RequiredArgsConstructor
public class LocationEnricher {

    private final LocationRepository locationRepository;
    private final LocationIqApiScraper locationIqApiScraper;
    private final LocationConfigRepository locationConfigRepository;
    private final Schedulers schedulers;
    private final SerialDatabaseWriter serialDatabaseWriter;

    public void enrich(Location location) {
        Optional<Location> existing = locationRepository.findByName(location.getName());
        if (existing.isEmpty() || !existing.get().isComplete()) {
            log.info("Location '{}' is not complete - will enrich it with more details", location.getName());
            Source source = locationIqApiScraper.getSource();
            Optional<LocationIqLocationConfig> locationConfig = locationConfigRepository.getLocationIqLocationConfig(location.getName());
            if (locationConfig.isPresent()) {
                Consumer<Location> locationProcessor = enrichedLocation -> {
                    log.info("Received enriched location: {}", enrichedLocation);
                    serialDatabaseWriter.execute(() -> {
                        locationRepository.save(enrichedLocation);
                    });
                };
                locationIqApiScraper.scheduleScraping(locationProcessor, schedulers, locationConfig.get());
            } else {
                log.error("Failed to get location config for source {} and location name {}. Cannot enrich location", source, location.getName());
            }
        }
    }

}
