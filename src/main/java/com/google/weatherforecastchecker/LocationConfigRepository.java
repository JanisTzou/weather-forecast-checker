package com.google.weatherforecastchecker;

import com.google.weatherforecastchecker.scraper.forecast.AccuWeatherLocationConfig;
import com.google.weatherforecastchecker.scraper.forecast.Source;
import com.google.weatherforecastchecker.scraper.measurement.ChmuLocationConfig;
import com.google.weatherforecastchecker.util.CsvFile;
import lombok.extern.log4j.Log4j2;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.google.weatherforecastchecker.LocationConfigRepository.Headers.*;

@Log4j2
public class LocationConfigRepository {

    static class Headers {
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

    private static final Map<String, Location> locationByNameMap = new ConcurrentHashMap<>();

    private static final String sourceConfigFilesFolder = "data_sources";
    private static final String locationsFile = "locations.csv";
    private static final String meteobluePictogramsMappingFile = "meteoblue_pictograms_mapping.csv";
    private static final Map<Source, Supplier<List<? extends LocationConfig>>> sourceConfigFiles = new ConcurrentHashMap<>();

    static {
        sourceConfigFiles.put(Source.ALADIN_API, () -> getLocationConfigs("aladin_api_config.csv"));
        sourceConfigFiles.put(Source.ACCUWATHER_API, () -> getAccuWeatherLocations("accuweather_api_config.csv"));
        sourceConfigFiles.put(Source.CLEAR_OUTSIDE_WEB, () -> getLocationConfigs("clearoutside_web_config.csv"));
        sourceConfigFiles.put(Source.METEOBLUE_API, () -> getLocationConfigs("meteoblue_api_config.csv"));
        sourceConfigFiles.put(Source.METEOBLUE_WEB, () -> getLocationConfigs("meteoblue_web_config.csv"));
        sourceConfigFiles.put(Source.CHMU, () -> getChmuLocationConfigs("chmu_measurements_web_config.csv"));
    }

    static {
        getLocations().forEach(l -> {
            locationByNameMap.put(l.getName(), l);
        });
    }

    @SuppressWarnings("unchecked")
    public static <T extends LocationConfig> List<T> getLocationConfigs(Source source) {
        Supplier<List<? extends LocationConfig>> supplier = sourceConfigFiles.get(source);
        if (supplier != null) {
            return (List<T>) supplier.get();
        } else {
            throw new IllegalArgumentException("No location configs for source = " + source);
        }
    }

    public static List<Location> getLocations() {
        return CsvFile.fromResourceFile(getPath(locationsFile)).getLines().stream()
                .map(l -> new Location(
                        l.getString(LOCATION_NAME),
                        l.getString(LATITUDE),
                        l.getString(LONGITUDE))
                ).collect(Collectors.toList());
    }

    public static List<LocationConfig> getLocationConfigs(String configFile) {
        return CsvFile.fromResourceFile(getPath(configFile)).getLines().stream()
                .map(l -> new LocationConfig(
                        getLocation(l.getString(LOCATION_NAME)),
                        l.getBoolean(ENABLED),
                        l.getTimes(SCRAPING_TIMES))
                ).collect(Collectors.toList());
    }

    public static List<AccuWeatherLocationConfig> getAccuWeatherLocations(String configFile) {
        Map<String, LocationConfig> configsByName = getLocationConfigsByName(configFile);
        return CsvFile.fromResourceFile(getPath(configFile)).getLines().stream()
                .map(l -> new AccuWeatherLocationConfig(
                        configsByName.get(l.getString(LOCATION_NAME)),
                        l.getString(ACCUWEATHER_LOCATION_KEY))
                ).collect(Collectors.toList());
    }

    public static List<ChmuLocationConfig> getChmuLocationConfigs(String configFile) {
        Map<String, Location> configsByName = getLocationsByName();
        return CsvFile.fromResourceFile(getPath(configFile)).getLines().stream()
                .map(l -> new ChmuLocationConfig(
                        configsByName.get(l.getString(LOCATION_NAME)),
                        l.getString(CHMU_STATION))
                ).collect(Collectors.toList());
    }

    public static Map<Integer, MeteobluePictorgramsConfig> getMeteobluePictogramsConfigs() {
        return CsvFile.fromResourceFile(getPath(meteobluePictogramsMappingFile)).getLines().stream()
                .map(l -> new MeteobluePictorgramsConfig(
                        l.getInt(PICTOGRAM_NO),
                        l.getString(PICTOGRAM_DESC),
                        l.getInt(PICTOGRAM_CLOUD_COVERAGE)
                )).collect(Collectors.toMap(MeteobluePictorgramsConfig::getPictogramId, m -> m));
    }

    // ===== private methods =====

    private static String getPath(String file) {
        return Path.of(sourceConfigFilesFolder, file).toString();
    }

    private static Map<String, LocationConfig> getLocationConfigsByName(String file) {
        return getLocationConfigs(file).stream().collect(Collectors.toMap(LocationConfig::getName, l -> l));
    }

    private static Map<String, Location> getLocationsByName() {
        return getLocations().stream().collect(Collectors.toMap(Location::getName, l -> l));
    }

    private static Location getLocation(String locationName) {
        Location location = locationByNameMap.get(locationName);
        if (location == null) {
            throw new IllegalArgumentException("Failed to find location for name " + locationName);
        }
        return location;
    }

}
