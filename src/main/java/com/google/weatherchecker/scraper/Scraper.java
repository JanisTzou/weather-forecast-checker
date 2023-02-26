package com.google.weatherchecker.scraper;

import com.google.weatherchecker.model.Source;

public interface Scraper<P extends ScrapingProps> {

    P getScrapingProps();

    Source getSource();

}
