package com.google.weatherforecastchecker.scraper;

import com.google.weatherforecastchecker.repos.*;
import com.google.weatherforecastchecker.scraper.forecast.Forecast;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

@Log4j2
@Component
@RequiredArgsConstructor
public class ForecastProcessor {

    private final SerialDatabaseWriter serialDatabaseWriter;
    private final ForecastRepositoryImpl forecastRepositoryImpl;

    // ensure single threaded writes ...
    public void processForecast(Forecast forecast) {
        log.info("Received forecast: {}", forecast);
        serialDatabaseWriter.execute(() -> forecastRepositoryImpl.save(forecast));
    }

}
