package com.google.weatherforecastchecker.scraper.measurement;

import com.google.weatherforecastchecker.scraper.forecast.AccuWeatherLocationKey;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

@Component
@Log4j2
public class CloudCoverageMeasurementsProcessor {

    public void processMeasurements(CloudCoverageMeasurements measurements) {
        log.info(measurements);
    }

}
