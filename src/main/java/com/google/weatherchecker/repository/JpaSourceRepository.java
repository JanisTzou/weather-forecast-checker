package com.google.weatherchecker.repository;

import com.google.weatherchecker.model.Source;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface JpaSourceRepository extends JpaRepository<JpaSource, Integer> {

    Optional<JpaSource> findFirstByName(Source name);

}
