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
    private final JpaLocationMapper mapper;

    @Override
    public Optional<Location> findByName(String name) {
        return jpaLocationRepository.findByName(name).map(mapper::toDomain);
    }

    @Transactional
    @Override
    public void save(Location location) {
        Optional<JpaLocation> oldLod = jpaLocationRepository.findByName(location.getName());
        JpaLocation newLoc = mapper.toEntity(location);
        if (oldLod.isPresent()) {
            oldLod.get().updateWith(newLoc);
            jpaLocationRepository.save(oldLod.get());
        } else {
            jpaLocationRepository.save(newLoc);
        }
    }

}
