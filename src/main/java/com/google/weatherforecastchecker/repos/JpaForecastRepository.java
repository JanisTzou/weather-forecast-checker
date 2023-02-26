package com.google.weatherforecastchecker.repos;

import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaForecastRepository extends JpaRepository<JpaForecast, Integer> {
}
