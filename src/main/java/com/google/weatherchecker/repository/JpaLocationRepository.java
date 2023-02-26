package com.google.weatherchecker.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface JpaLocationRepository extends JpaRepository<JpaLocation, Integer>,
        JpaLocationRepositorySavingNewOnly {

    Optional<JpaLocation> findByName(String name);

}
