package com.google.weatherchecker.model;

public enum Source {

    ACCUWATHER_API(Provider.ACCUWATHER),
    ALADIN_API(Provider.ALADIN),
    CLEAR_OUTSIDE_WEB(Provider.CLEAR_OUTSIDE),
    METEOBLUE_WEB(Provider.METEOBLUE),
    METEOBLUE_API(Provider.METEOBLUE),
    CHMU_WEB(Provider.CHMU),
    LOCATION_IQ_API(Provider.LOCATION_IQ);

    private final Provider provider;

    Source(Provider provider) {
        this.provider = provider;
    }

    public Provider getProvider() {
        return provider;
    }

}
