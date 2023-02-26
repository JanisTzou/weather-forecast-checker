package com.google.weatherforecastchecker.repos;

import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaHourlyForecastRepository extends JpaRepository<JpaHourlyForecast, Integer> {
}
