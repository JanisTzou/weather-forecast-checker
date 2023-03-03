package com.google.weatherchecker.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface JpaCountyRepository extends JpaRepository<JpaCounty, Integer> {

    Optional<JpaCounty> findFirstByName(String name);

}
