package com.google.weatherchecker.model;

import java.util.Optional;

public enum Source {

    ACCUWATHER_API(Provider.ACCUWATHER, "AccuWeather", "AccuWeather"),
    ALADIN_API(Provider.ALADIN, "Aladin", "Aladin"),
    CLEAR_OUTSIDE_WEB(Provider.CLEAR_OUTSIDE, "ClearOutside", "ClearOutside"),
    METEOBLUE_WEB(Provider.METEOBLUE, "Meteoblue WEB", "Meteoblue WEB"),
    METEOBLUE_API(Provider.METEOBLUE, "Meteoblue API", "Meteoblue API"),
    MET_NORWAY_API(Provider.MET_NORWAY, "MET Norway", "MET Norway"),
    OPEN_METEO_ECMWF(Provider.OPEN_METEO, "ECMWF", "ECMWF"),
    CHMU_WEB(Provider.CHMU, null, null),
    LOCATION_IQ_API(Provider.LOCATION_IQ, null, null);

    private final Provider provider;
    private final String publicName;
    private final String adminName;

    Source(Provider provider, String publicName, String adminName) {
        this.provider = provider;
        this.publicName = publicName;
        this.adminName = adminName;
    }

    public Provider getProvider() {
        return provider;
    }

    public Optional<String> getPublicName() {
        return Optional.ofNullable(publicName);
    }

    public Optional<String> getAdminName() {
        return Optional.ofNullable(adminName);
    }
}
