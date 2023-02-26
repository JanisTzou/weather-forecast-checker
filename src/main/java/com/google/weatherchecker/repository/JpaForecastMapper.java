package com.google.weatherchecker.repository;

import com.google.weatherchecker.model.Forecast;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

@Log4j2
@Component
@RequiredArgsConstructor
public class JpaForecastMapper {

    private final JpaSourceMapper jpaSourceMapper;
    private final JpaLocationMapper jpaLocationMapper;

    public Forecast toDomain(JpaForecast jpaForecast) {
        return null;
    }

    public JpaForecast toEntity(Forecast forecast) {
        JpaSource jpaSource = jpaSourceMapper.toEntity(forecast.getSource());
        JpaLocation jpaLocation = jpaLocationMapper.toEntity(forecast.getLocation());
        return new JpaForecast(
                forecast.getScraped(),
                jpaSource,
                jpaLocation
        );
    }

}
