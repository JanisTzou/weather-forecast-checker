package com.google.weatherforecastchecker.scraper;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
public abstract class ScrapingProps implements TimedScraping {

    protected String url;
    protected boolean enabled;

}
