package com.google.weatherchecker.repository;

import com.google.weatherchecker.model.Forecast;
import com.google.weatherchecker.model.HourlyForecast;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Log4j2
@Repository
@RequiredArgsConstructor
public class ForecastRepositoryImpl implements ForecastRepository {

    private final JpaLocationRepository jpaLocationRepository;
    private final JpaForecastMapper jpaForecastMapper;
    private final JpaHourlyForecastMapper jpaHourlyForecastMapper;
    private final JpaHourlyForecastRepository jpaHourlyForecastRepository;
    private final JpaSourceRepository jpaSourceRepository;

    @Override
    @Transactional
    public void save(Forecast forecast) {
        JpaForecast jpaForecast = jpaForecastMapper.toEntity(forecast);
        Optional<JpaSource> jpaSource = jpaSourceRepository.findFirstByName(forecast.getSource());
        if (jpaSource.isPresent()) {
            JpaLocation jpaLocation = jpaLocationRepository.saveIfNewAndGet(jpaForecast.getLocation());
            jpaForecast.setLocation(jpaLocation);
            jpaForecast.setSource(jpaSource.get());
            for (HourlyForecast hourlyForecast : forecast.getHourlyForecasts()) {
                JpaHourlyForecast jpaHourlyForecast = jpaHourlyForecastMapper.mapToEntity(hourlyForecast, jpaForecast);
                jpaHourlyForecastRepository.save(jpaHourlyForecast);
            }
        } else {
            log.error("Failed to save forecast {}", forecast);
        }

    }

}
