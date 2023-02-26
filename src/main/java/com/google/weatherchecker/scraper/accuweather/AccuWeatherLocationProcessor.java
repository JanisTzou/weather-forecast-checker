package com.google.weatherchecker.scraper.accuweather;

import org.springframework.stereotype.Component;

@Component
public class AccuWeatherLocationProcessor {

    public void processLocationKey(AccuWeatherLocationKey locationKey) {
        System.out.println("LOCATION=" + locationKey.getLocation().getName() + ",KEY=" + locationKey.getLocationKey());
    }

}
