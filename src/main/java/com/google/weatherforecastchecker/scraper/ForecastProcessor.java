package com.google.weatherforecastchecker.scraper;

import com.google.weatherforecastchecker.scraper.forecast.Forecast;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

@Log4j2
@Component
public class ForecastProcessor {

    public void processForecast(Forecast forecast) {
        log.info("Received forecast: {}", forecast);
    }

}
