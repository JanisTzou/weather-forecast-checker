package com.google.weatherforecastchecker.repos;

import com.google.weatherforecastchecker.scraper.Source;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface JpaSourceRepository extends JpaRepository<JpaSource, Integer> {

    Optional<JpaSource> findFirstByName(Source name);

}
