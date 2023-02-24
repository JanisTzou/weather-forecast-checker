package com.google.weatherforecastchecker.scraper.forecast;

public enum Source {

    ACCUWATHER_API(Provider.ACCUWATHER),
    ALADIN_API(Provider.ALADIN),
    CLEAR_OUTSIDE_WEB(Provider.CLEAR_OUTSIDE),
    METEOBLUE_WEB(Provider.METEOBLUE),
    METEOBLUE_API(Provider.METEOBLUE),
    CHMU(Provider.CHMU);

    private final Provider provider;

    Source(Provider provider) {
        this.provider = provider;
    }

    public Provider getProvider() {
        return provider;
    }

}
