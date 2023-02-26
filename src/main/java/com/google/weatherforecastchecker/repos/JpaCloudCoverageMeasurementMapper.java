package com.google.weatherforecastchecker.repos;

import com.google.weatherforecastchecker.scraper.measurement.CloudCoverageMeasurement;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Log4j2
@Component
@RequiredArgsConstructor
public class JpaCloudCoverageMeasurementMapper {

    private final JpaLocationMapper jpaLocationMapper;
    private final JpaSourceMapper jpaSourceMapper;

    public CloudCoverageMeasurement toDomain(JpaCloudCoverageMeasurement jpaMeasurement) {
        return null;
    }

    public JpaCloudCoverageMeasurement toEntity(CloudCoverageMeasurement measurement) {
        JpaSource jpaSource = jpaSourceMapper.toEntity(measurement.getSource());
        return new JpaCloudCoverageMeasurement(
                measurement.getScraped(),
                measurement.getDateTime(),
                jpaLocationMapper.toEntity(measurement.getLocation()),
                jpaSource,
                measurement.getCloudCoverage(),
                measurement.getDescription()
        );
    }

}
