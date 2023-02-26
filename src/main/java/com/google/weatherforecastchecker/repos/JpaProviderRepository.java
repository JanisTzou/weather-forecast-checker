package com.google.weatherforecastchecker.repos;

import com.google.weatherforecastchecker.scraper.Provider;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaProviderRepository extends JpaRepository<JpaProvider, Integer> {

}
