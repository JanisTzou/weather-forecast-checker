package com.google.weatherforecastchecker.repos;

import com.google.weatherforecastchecker.scraper.Location;
import org.springframework.stereotype.Component;

@Component
public class JpaLocationMapper {

    public Location toDomain(JpaLocation jpaLocation) {
        return new Location(jpaLocation.getName(), jpaLocation.getLatitude(), jpaLocation.getLongitude());
    }

    public JpaLocation toEntity(Location location) {
        return new JpaLocation(location.getName(), location.getLatitude(), location.getLongitude());
    }

}
