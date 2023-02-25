package com.google.weatherforecastchecker.scraper.forecast;

import com.google.weatherforecastchecker.scraper.Location;
import lombok.Data;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class AccuWeatherLocationProcessor {

    public void processLocationKey(AccuWeatherLocationKey locationKey) {
        System.out.println("LOCATION=" + locationKey.getLocation().getName() + ",KEY=" + locationKey.getLocationKey());
    }

}
