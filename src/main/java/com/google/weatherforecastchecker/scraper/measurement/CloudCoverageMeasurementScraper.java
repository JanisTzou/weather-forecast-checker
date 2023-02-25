package com.google.weatherforecastchecker.scraper.measurement;

import com.google.weatherforecastchecker.scraper.Scraper;
import com.google.weatherforecastchecker.scraper.ScrapingProps;

import java.util.List;

public interface CloudCoverageMeasurementScraper<P extends ScrapingProps> extends Scraper<P> {

    List<CloudCoverageMeasurement> scrape();

}
