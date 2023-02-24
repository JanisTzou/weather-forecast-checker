package com.google.weatherforecastchecker;

import com.google.weatherforecastchecker.scraper.forecast.AccuWeatherLocationConfig;
import com.google.weatherforecastchecker.scraper.forecast.Source;
import com.google.weatherforecastchecker.scraper.measurement.ChmuLocationConfig;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Log4j2
public class LocationConfigRepository {

    public static final String no_value = "-";

    private static final int LOC_NAME = 0;
    private static final int LAT = 1;
    private static final int LON = 2;

    private static final int ENABLED = 1;
    private static final int SCRAPE_AT_TIMES = 2;

    private static final int ACCUWEATHER_LOCATION_KEY = 3;
    private static final int CHMU_STATION = 1;

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
        return readAndSplitConfigCsv(getPath(locationsFile)).stream()
                .map(s -> new Location(s.get(LOC_NAME), s.get(LAT), s.get(LON)))
                .collect(Collectors.toList());
    }

    public static List<LocationConfig> getLocationConfigs(String configFile) {
        return readAndSplitConfigCsv(getPath(configFile)).stream()
                .map(s -> {
                    Location location = getLocation(s.get(LOC_NAME));
                    List<LocalTime> scrapingTimes = parseScrapingTimes(s.get(SCRAPE_AT_TIMES));
                    boolean enabled = Boolean.parseBoolean(s.get(ENABLED));
                    return new LocationConfig(location, enabled, scrapingTimes);
                })
                .collect(Collectors.toList());
    }

    public static List<AccuWeatherLocationConfig> getAccuWeatherLocations(String configFile) {
        Map<String, LocationConfig> configsByName = getLocationConfigsByName(configFile);
        return readAndSplitConfigCsv(getPath(configFile)).stream()
                .map(s -> {
                    LocationConfig config = configsByName.get(s.get(LOC_NAME));
                    return new AccuWeatherLocationConfig(config, s.get(ACCUWEATHER_LOCATION_KEY));
                })
                .collect(Collectors.toList());
    }

    public static List<ChmuLocationConfig> getChmuLocationConfigs(String configFile) {
        Map<String, Location> configsByName = getLocationsByName();
        return readAndSplitConfigCsv(getPath(configFile)).stream()
                .map(s -> {
                    Location config = configsByName.get(s.get(LOC_NAME));
                    return new ChmuLocationConfig(config, s.get(CHMU_STATION));
                })
                .collect(Collectors.toList());
    }

    public static Map<Integer, MeteobluePictorgramsConfig> getMeteobluePictogramsConfigs() {
        return readAndSplitConfigCsv(getPath(meteobluePictogramsMappingFile)).stream()
                .map(split -> new MeteobluePictorgramsConfig(Integer.parseInt(split.get(0)), split.get(1), Integer.parseInt(split.get(2))))
                .collect(Collectors.toMap(MeteobluePictorgramsConfig::getPictogramId, m -> m));
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

    private static List<LocalTime> parseScrapingTimes(String timesStr) {
        if (no_value.equals(timesStr)) {
            return Collections.emptyList();
        } else {
            return Utils.parseScrapingTimes(timesStr);
        }
    }

    private static Location getLocation(String locationName) {
        Location location = locationByNameMap.get(locationName);
        if (location == null) {
            throw new IllegalArgumentException("Failed to find location for name " + locationName);
        }
        return location;
    }

    private static List<List<String>> readAndSplitConfigCsv(String resourcesFile) {
        try {
            return Utils.readResourcesFileLines(resourcesFile).stream()
                    .skip(1L)
                    .map(line -> {
                        String[] items = line.trim().split("\\|");
                        return Arrays.asList(items);
                    }).collect(Collectors.toList());
        } catch (IOException e) {
            log.error("Failed to read configuration file!", e);
            return Collections.emptyList();
        }
    }

}
