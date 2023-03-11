package com.google.weatherchecker.repository;

import com.google.weatherchecker.model.ForecastVerification;

import java.time.LocalDate;
import java.util.List;

public interface ForecastVerificationRepository {

    void save(List<ForecastVerification> verifications);

    void save(ForecastVerification forecastVerification);

    List<ForecastVerification> calculateVerifications(VerificationCriteria criteria);

    List<ForecastVerification> findVerifications(VerificationCriteria criteria);

    List<LocalDate> getMissingDailyVerificationDates();

    void createDailyVerifications();

    void updatePastHoursVerifications();

}
