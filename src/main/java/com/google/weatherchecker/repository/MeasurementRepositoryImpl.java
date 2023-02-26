package com.google.weatherchecker.repository;

import com.google.weatherchecker.model.CloudCoverageMeasurement;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Log4j2
@Repository
@RequiredArgsConstructor
public class MeasurementRepositoryImpl implements MeasurementRepository {

    private final JpaCloudCoverageMeasurementRepository jpaCloudCoverageMeasurementRepository;
    private final JpaLocationRepository jpaLocationRepository;
    private final JpaSourceRepository jpaSourceRepository;
    private final JpaCloudCoverageMeasurementMapper measurementMapper;

    @Transactional
    @Override
    public void save(CloudCoverageMeasurement measurement) {
        JpaCloudCoverageMeasurement jpaMeasurement = measurementMapper.toEntity(measurement);
        Optional<JpaSource> jpaSource = jpaSourceRepository.findFirstByName(measurement.getSource());
        if (jpaSource.isPresent()) {
            JpaLocation jpaLocation = jpaLocationRepository.saveIfNewAndGet(jpaMeasurement.getLocation());
            jpaMeasurement.setLocation(jpaLocation);
            jpaMeasurement.setSource(jpaSource.get());
            jpaCloudCoverageMeasurementRepository.save(jpaMeasurement);
        } else {
            log.error("Failed to save measurement: {}", measurement);
        }
    }
}
