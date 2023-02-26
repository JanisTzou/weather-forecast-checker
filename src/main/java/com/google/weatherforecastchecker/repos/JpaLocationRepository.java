package com.google.weatherforecastchecker.repos;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface JpaLocationRepository extends JpaRepository<JpaLocation, Integer>,
        JpaLocationRepositorySavingNewOnly {

    Optional<JpaLocation> findByName(String name);

}
