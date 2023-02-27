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
                jpaLocation.getMunicipality(),
                jpaLocation.getCounty(),
                jpaLocation.getRegion(),
                jpaLocation.isComplete()
        );
    }

    public JpaLocation toEntity(Location location) {
        return new JpaLocation(location.getName(),
                location.getLatitude(),
                location.getLongitude(),
                location.getMunicipality(),
                location.getCounty(),
                location.getRegion(),
                location.isComplete()
        );
    }

}
