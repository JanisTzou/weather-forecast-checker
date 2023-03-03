package com.google.weatherchecker.repository;

import com.google.weatherchecker.model.Location;
import org.springframework.stereotype.Component;

@Component
public class JpaLocationMapper {

    public Location toDomain(JpaLocation jpaLocation) {
        return new Location(
                jpaLocation.getName(),
                jpaLocation.getLatitude(),
                jpaLocation.getLongitude(),
                jpaLocation.getMunicipality().map(JpaMunicipality::getName).orElse(null),
                jpaLocation.getCounty().map(JpaCounty::getName).orElse(null),
                jpaLocation.getRegion().map(JpaRegion::getName).orElse(null),
                jpaLocation.isComplete()
        );
    }

    public JpaLocation toEntity(Location location) {
        return new JpaLocation(location.getName(),
                location.getLatitude(),
                location.getLongitude(),
                location.getMunicipality().map(JpaMunicipality::new).orElse(null),
                location.getCounty().map(JpaCounty::new).orElse(null),
                location.getRegion().map(JpaRegion::new).orElse(null),
                location.isComplete()
        );
    }

}
