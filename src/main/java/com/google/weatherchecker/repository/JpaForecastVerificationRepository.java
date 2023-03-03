package com.google.weatherchecker.repository;

import com.google.weatherchecker.model.ForecastVerificationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

public interface JpaForecastVerificationRepository extends JpaRepository<JpaForecastVerification, Integer> {

    void deleteAllByTypeName(ForecastVerificationType type_name);

    @Transactional
    @Modifying
    @Query("select p.id from JpaForecastVerification p where p.type.name = ?1")
    List<Integer> findAllIdsByType(ForecastVerificationType type);

    List<JpaForecastVerification> findAllByPastHoursAndDayAndCountyNameAndRegionName(Integer pastHours,
                                                                                     LocalDate day,
                                                                                     String county,
                                                                                     String region);

    @Transactional
    void deleteAllByPastHoursAndCountyNameAndRegionName(Integer pastHours,
                                                        String county,
                                                        String region);
}
