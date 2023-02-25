package com.google.weatherforecastchecker.scraper;

import com.google.weatherforecastchecker.scraper.forecast.AccuWeatherApiLocationConfig;
import com.google.weatherforecastchecker.scraper.forecast.MeteobluePictorgramsConfig;
import com.google.weatherforecastchecker.scraper.measurement.ChmuLocationConfig;
import com.google.weatherforecastchecker.util.CsvFile;
import jakarta.annotation.PostConstruct;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.google.weatherforecastchecker.scraper.LocationConfigRepository.CsvConfigsHeaders.*;

@Component
@Log4j2
public class LocationConfigRepository {

    private static final String sourceConfigFilesFolder = "data_sources";
    private static final String locationsFile = "locations.csv";
    private static final String meteobluePictogramsMappingFile = "meteoblue_pictograms_mapping.csv";
    private final Map<Source, Supplier<List<LocationConfig>>> locationsConfigsLoaders = new ConcurrentHashMap<>();

    @PostConstruct
    private void init() {
        locationsConfigsLoaders.put(Source.ACCUWATHER_LOCATIONS_API, () -> getLocationConfigs("accuweather_locations_api_config.csv"));
        locationsConfigsLoaders.put(Source.ALADIN_API, () -> getLocationConfigs("aladin_api_config.csv"));
        locationsConfigsLoaders.put(Source.CLEAR_OUTSIDE_WEB, () -> getLocationConfigs("clearoutside_web_config.csv"));
        locationsConfigsLoaders.put(Source.METEOBLUE_API, () -> getLocationConfigs("meteoblue_api_config.csv"));
        locationsConfigsLoaders.put(Source.METEOBLUE_WEB, () -> getLocationConfigs("meteoblue_web_config.csv"));
    }

    public List<AccuWeatherApiLocationConfig> getAccuWeatherLocationConfigs() {
        return getAccuWeatherLocationConfigs("accuweather_api_config.csv");
    }

    public List<ChmuLocationConfig> getChmuLocationConfigs() {
        return getChmuLocationConfigs("chmu_measurements_web_config.csv");
    }

    public List<LocationConfig> getLocationConfigs(Source source) {
        Supplier<List<LocationConfig>> supplier = locationsConfigsLoaders.get(source);
        if (supplier != null) {
            return supplier.get();
        } else {
            throw new IllegalArgumentException("No location configs for source = " + source);
        }
    }

    List<Location> getLocations() {
        return CsvFile.fromResourceFile(getPath(locationsFile)).getLines().stream()
                .map(l -> new Location(
                        l.getString(LOCATION_NAME),
                        l.getString(LATITUDE),
                        l.getString(LONGITUDE))
                ).collect(Collectors.toList());
    }

    List<LocationConfig> getLocationConfigs(String configFile) {
        Map<String, Location> locationsByName = getLocationsByName();
        return CsvFile.fromResourceFile(getPath(configFile)).getLines().stream()
                .map(l -> new LocationConfig(
                        getLocation(locationsByName, l.getString(LOCATION_NAME)),
                        l.getBoolean(ENABLED),
                        l.getTimes(SCRAPING_TIMES))
                ).collect(Collectors.toList());
    }

    List<AccuWeatherApiLocationConfig> getAccuWeatherLocationConfigs(String configFile) {
        Map<String, LocationConfig> configsByName = getLocationConfigsByName(configFile);
        return CsvFile.fromResourceFile(getPath(configFile)).getLines().stream()
                .map(l -> new AccuWeatherApiLocationConfig(
                        configsByName.get(l.getString(LOCATION_NAME)),
                        l.getString(ACCUWEATHER_LOCATION_KEY))
                ).collect(Collectors.toList());
    }

    List<ChmuLocationConfig> getChmuLocationConfigs(String configFile) {
        Map<String, Location> configsByName = getLocationsByName();
        return CsvFile.fromResourceFile(getPath(configFile)).getLines().stream()
                .map(l -> new ChmuLocationConfig(
                        configsByName.get(l.getString(LOCATION_NAME)),
                        l.getString(CHMU_STATION))
                ).collect(Collectors.toList());
    }

    public Map<Integer, MeteobluePictorgramsConfig> getMeteobluePictogramsConfigs() {
        return CsvFile.fromResourceFile(getPath(meteobluePictogramsMappingFile)).getLines().stream()
                .map(l -> new MeteobluePictorgramsConfig(
                        l.getInt(PICTOGRAM_NO),
                        l.getString(PICTOGRAM_DESC),
                        l.getInt(PICTOGRAM_CLOUD_COVERAGE)
                )).collect(Collectors.toMap(MeteobluePictorgramsConfig::getPictogramId, m -> m));
    }

    private String getPath(String file) {
        return Path.of(sourceConfigFilesFolder, file).toString();
    }

    // ===== private methods =====

    private Map<String, LocationConfig> getLocationConfigsByName(String file) {
        return getLocationConfigs(file).stream().collect(Collectors.toMap(LocationConfig::getName, l -> l));
    }

    private Map<String, Location> getLocationsByName() {
        return getLocations().stream().collect(Collectors.toMap(Location::getName, l -> l));
    }

    private Location getLocation(Map<String, Location> locationsByName, String locationName) {
        Location location = locationsByName.get(locationName);
        if (location == null) {
            throw new IllegalArgumentException("Failed to find location for name " + locationName);
        }
        return location;
    }

    static class CsvConfigsHeaders {
        static final String LOCATION_NAME = "Location";
        static final String LATITUDE = "Latitude";
        static final String LONGITUDE = "Longitude";

        static final String ENABLED = "Enabled";
        static final String SCRAPING_TIMES = "ScrapingTimes";

        static final String ACCUWEATHER_LOCATION_KEY = "locationKey";
        static final String CHMU_STATION = "Station";

        static final String PICTOGRAM_NO = "Nr";
        static final String PICTOGRAM_DESC = "Description";
        static final String PICTOGRAM_CLOUD_COVERAGE = "Total Cloud Coverage";
    }

}
