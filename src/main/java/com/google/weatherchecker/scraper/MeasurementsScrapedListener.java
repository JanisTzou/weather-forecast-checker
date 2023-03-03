package com.google.weatherchecker.scraper;

import com.google.weatherchecker.model.Location;

public interface MeasurementsScrapedListener {

    void onAllLocationsScraped();

    void onLocationScraped(Location location);

}
