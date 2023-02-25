package com.google.weatherforecastchecker.scraper;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Duration;

@Getter
@Setter
@NoArgsConstructor
public abstract class LocationScrapingProps extends ScrapingProps {

    private Duration delayBetweenLocations;

}
