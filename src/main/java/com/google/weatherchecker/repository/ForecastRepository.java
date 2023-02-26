package com.google.weatherchecker.repository;

import com.google.weatherchecker.model.Forecast;

public interface ForecastRepository {

    void save(Forecast forecast);

}
