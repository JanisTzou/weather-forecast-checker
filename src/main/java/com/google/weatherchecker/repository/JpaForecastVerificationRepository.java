package com.google.weatherchecker.repository;

import com.google.weatherchecker.model.ForecastVerificationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

public interface JpaForecastVerificationRepository extends JpaRepository<JpaForecastVerification, Integer> {

    @Transactional
    @Modifying
    @Query("select p.id from JpaForecastVerification p where p.type.name = ?1")
    List<Integer> findAllIdsByType(ForecastVerificationType type);

    List<JpaForecastVerification> findAllByDayAndCountyIsNull(LocalDate day);

    List<JpaForecastVerification> findAllByDayAndCountyNameIn(LocalDate day,
                                                              List<String> counties);

    List<JpaForecastVerification> findAllByPastHoursAndCountyIsNull(Integer pastHours);

    List<JpaForecastVerification> findAllByPastHoursAndCountyNameIn(Integer pastHours,
                                                                    List<String> counties);

    List<JpaForecastVerification> findAllByDayBetweenAndCountyIsNullOrderByDay(LocalDate fromDate,
                                                                               LocalDate toDate);

    List<JpaForecastVerification> findAllByDayBetweenAndCountyNameInOrderByDay(LocalDate fromDate,
                                                                               LocalDate toDate,
                                                                               List<String> counties);

    @Transactional
    void deleteAllByPastHoursAndCountyName(Integer pastHours,
                                           String county);
}
