package com.google.weatherchecker.repository;

import com.google.weatherchecker.model.CloudCoverageMeasurement;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

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
                measurement.getCloudCoverageTotal(),
                measurement.getDescription()
        );
    }

}
