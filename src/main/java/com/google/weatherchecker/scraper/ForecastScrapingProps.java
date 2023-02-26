package com.google.weatherchecker.scraper;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public abstract class ForecastScrapingProps extends LocationScrapingProps {

    private int days;

}
