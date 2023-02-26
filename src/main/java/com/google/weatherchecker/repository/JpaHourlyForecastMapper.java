package com.google.weatherchecker.repository;

import com.google.weatherchecker.model.HourlyForecast;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

@Log4j2
@Component
@RequiredArgsConstructor
public class JpaHourlyForecastMapper {

    private final JpaSourceMapper jpaSourceMapper;
    private final JpaLocationMapper jpaLocationMapper;

    public HourlyForecast toDomain(JpaHourlyForecast jpaHourlyForecast) {
        return null;
    }

    public JpaHourlyForecast mapToEntity(HourlyForecast hourlyForecast, JpaForecast jpaForecast) {
        return new JpaHourlyForecast(
                hourlyForecast.getHour(),
                hourlyForecast.getCloudCoverTotal(),
                hourlyForecast.getDescription(),
                jpaForecast
        );
    }

}
