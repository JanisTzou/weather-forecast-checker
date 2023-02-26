package com.google.weatherchecker.repository;

public interface JpaLocationRepositorySavingNewOnly {

    JpaLocation saveIfNewAndGet(JpaLocation entity);

}
