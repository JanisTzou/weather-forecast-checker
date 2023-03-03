package com.google.weatherchecker.repository;

import com.google.weatherchecker.model.Location;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Log4j2
@Repository
@RequiredArgsConstructor
public class LocationRepositoryImpl implements LocationRepository {

    private final JpaLocationRepository jpaLocationRepository;
    private final JpaMunicipalityRepository jpaMunicipalityRepository;
    private final JpaCountyRepository jpaCountyRepository;
    private final JpaRegionRepository jpaRegionRepository;
    private final JpaLocationMapper mapper;

    @Override
    public Optional<Location> findByName(String name) {
        return jpaLocationRepository.findByName(name).map(mapper::toDomain);
    }

    // TODO look into this to solve unique constaints better with jpa: https://www.baeldung.com/jpa-unique-constraints
    @Transactional
    @Override
    public void save(Location location) {
        Optional<JpaLocation> oldLod = jpaLocationRepository.findByName(location.getName());
        JpaLocation newLoc = mapper.toEntity(location);
        location.getMunicipality().flatMap(jpaMunicipalityRepository::findFirstByName)
                .ifPresent(newLoc::setMunicipality);
        location.getCounty().flatMap(jpaCountyRepository::findFirstByName)
                .ifPresent(newLoc::setCounty);
        location.getRegion().flatMap(jpaRegionRepository::findFirstByName)
                .ifPresent(newLoc::setRegion);
        if (oldLod.isPresent()) {
            oldLod.get().updateWith(newLoc);
            jpaLocationRepository.save(oldLod.get());
        } else {
            jpaLocationRepository.save(newLoc);
        }
    }

}
