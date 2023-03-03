package com.google.weatherchecker.repository;

import com.google.weatherchecker.model.ForecastVerificationType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface JpaForecastVerificationTypeRepository extends JpaRepository<JpaForecastVerificationType, Integer> {

    Optional<JpaForecastVerificationType> findFirstByName(ForecastVerificationType name);

}
