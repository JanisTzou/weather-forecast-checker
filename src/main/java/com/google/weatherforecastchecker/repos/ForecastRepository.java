package com.google.weatherforecastchecker.repos;

import com.google.weatherforecastchecker.scraper.forecast.Forecast;

public interface ForecastRepository {

    void save(Forecast forecast);

}
