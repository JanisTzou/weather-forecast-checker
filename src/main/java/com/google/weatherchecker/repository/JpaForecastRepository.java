package com.google.weatherchecker.repository;

import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaForecastRepository extends JpaRepository<JpaForecast, Integer> {
}
