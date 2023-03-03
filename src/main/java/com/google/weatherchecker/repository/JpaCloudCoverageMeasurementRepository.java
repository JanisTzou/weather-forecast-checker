package com.google.weatherchecker.repository;

import com.google.weatherchecker.model.Source;
import org.springframework.data.domain.Example;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;

public interface JpaCloudCoverageMeasurementRepository extends JpaRepository<JpaCloudCoverageMeasurement, Integer> {
}
