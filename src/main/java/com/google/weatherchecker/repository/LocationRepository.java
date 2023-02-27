package com.google.weatherchecker.repository;

import com.google.weatherchecker.model.Location;

import java.util.Optional;

public interface LocationRepository {

    Optional<Location> findByName(String name);

    void save(Location location);

}
