package com.google.weatherchecker.verification;

import com.google.weatherchecker.model.Location;
import com.google.weatherchecker.repository.ForecastVerificationRepository;
import com.google.weatherchecker.scraper.MeasurementsScrapedListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

@Log4j2
@Component
@RequiredArgsConstructor
public class VerificationGenerator implements MeasurementsScrapedListener {

    private final ForecastVerificationRepository forecastVerificationRepository;

    @Override
    public void onAllLocationsScraped() {
        forecastVerificationRepository.createDailyVerifications();
        forecastVerificationRepository.updatePastHoursVerifications();
    }

    @Override
    public void onLocationScraped(Location location) {
        log.warn("Not Implemented!");
    }

}
