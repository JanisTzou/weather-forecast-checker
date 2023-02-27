package com.google.weatherchecker.scraper;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Duration;

@Setter
@Getter
@NoArgsConstructor
public abstract class ScrapingProps implements TimedScraping {

    protected String url;
    protected boolean enabled;
    protected boolean scrapeOnceImmediately;
    protected boolean scheduleScraping;
    protected Duration delayBetweenRequests;

}
