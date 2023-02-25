package com.google.weatherforecastchecker.scraper;

public interface Scraper<P extends ScrapingProps> {

    P getScrapingProps();

    Source getSource();

}
