package com.google.weatherforecastchecker.repos;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface JpaLocationRepositorySavingNewOnly {

    JpaLocation saveIfNewAndGet(JpaLocation entity);

}
