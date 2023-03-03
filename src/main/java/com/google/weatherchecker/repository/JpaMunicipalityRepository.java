package com.google.weatherchecker.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface JpaMunicipalityRepository extends JpaRepository<JpaMunicipality, Integer> {

    Optional<JpaMunicipality> findFirstByName(String name);

}
