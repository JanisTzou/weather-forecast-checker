package com.google.weatherchecker.repository;

import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaHourlyForecastRepository extends JpaRepository<JpaHourlyForecast, Integer> {
}
