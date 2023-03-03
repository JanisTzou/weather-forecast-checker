package com.google.weatherchecker.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface JpaRegionRepository extends JpaRepository<JpaRegion, Integer> {

    Optional<JpaRegion> findFirstByName(String name);

}
