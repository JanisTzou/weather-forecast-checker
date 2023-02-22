package com.google.weatherforecastchecker;

import com.google.weatherforecastchecker.scraper.forecast.AccuWeatherLocation;
import com.google.weatherforecastchecker.scraper.measurement.ChmuLocation;
import lombok.Data;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Log4j2
public class LocationsReader {

    public static final String LOCATIONS_CONFIG = "/Users/janis/IdeaProjects/weather-forecast-checker/src/main/resources/locations_config.csv";
    public static final String METEOBLUE_PICTOGRAMS = "/Users/janis/IdeaProjects/weather-forecast-checker/src/main/resources/meteoblue_pictograms.csv";

    private static final int LOC = 0;
    private static final int CHMU_LOC = 1;
    private static final int LAT = 2;
    private static final int LON = 3;
    private static final int ACC_LOC = 4;

    public static List<Location> getLocations() {
        return readAndSplitConfigCsv(LOCATIONS_CONFIG).stream()
                .map(split -> new Location(split.get(LOC), split.get(LAT), split.get(LON)))
                .collect(Collectors.toList());
    }

    public static List<AccuWeatherLocation> getAccuWeatherLocations() {
        return readAndSplitConfigCsv(LOCATIONS_CONFIG).stream()
                .map(split -> new AccuWeatherLocation(split.get(LOC), split.get(LAT), split.get(LON), split.size() >= ACC_LOC + 1 ? split.get(ACC_LOC) : null))
                .collect(Collectors.toList());
    }

    public static List<ChmuLocation> getChmuLocations() {
        return readAndSplitConfigCsv(LOCATIONS_CONFIG).stream()
                .map(split -> new ChmuLocation(split.get(LOC), split.get(LAT), split.get(LON), split.get(CHMU_LOC)))
                .collect(Collectors.toList());
    }

    public static Map<Integer, MeteobluePictorgramsConfig> getMeteobluePictogramsConfigs() {
        return readAndSplitConfigCsv(METEOBLUE_PICTOGRAMS).stream()
                .map(split -> new MeteobluePictorgramsConfig(Integer.parseInt(split.get(0)), split.get(1), Integer.parseInt(split.get(2))))
                .collect(Collectors.toMap(MeteobluePictorgramsConfig::getPictogramId, m -> m));
    }

    private static List<List<String>> readAndSplitConfigCsv(String configFile) {
        try {
            return FileUtils.readLines(new File(configFile), Charset.defaultCharset())
                    .stream()
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

    @Data
    public static class MeteobluePictorgramsConfig {
        private final int pictogramId;
        private final String description;
        private final int cloudCoverage;

    }

}
