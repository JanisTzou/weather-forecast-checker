package com.google.weatherforecastchecker.repos;

import com.google.weatherforecastchecker.scraper.forecast.Forecast;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import java.util.Optional;

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
