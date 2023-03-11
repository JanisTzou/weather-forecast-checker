package com.google.weatherchecker.scraper;

import com.google.weatherchecker.model.Location;
import com.google.weatherchecker.model.Source;
import com.google.weatherchecker.scraper.accuweather.AccuWeatherLocationConfig;
import com.google.weatherchecker.scraper.chmu.ChmuLocationConfig;
import com.google.weatherchecker.scraper.locationiq.LocationIqLocationConfig;
import com.google.weatherchecker.scraper.meteoblue.MeteobluePictorgramsConfig;
import com.google.weatherchecker.util.CsvFile;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.google.weatherchecker.scraper.LocationConfigRepository.CsvConfigsHeaders.*;

@Component
@Log4j2
public class LocationConfigRepository {

    private static final String sourceConfigFilesFolder = "scraping_configs";
    private static final String locationsFile = "locations.csv";
    private static final String meteobluePictogramsMappingFile = "meteoblue_pictograms_mapping.csv";
    private final Map<Source, Supplier<List<LocationConfig>>> locationsConfigsLoaders = new ConcurrentHashMap<>();

    @PostConstruct
    private void init() {
        locationsConfigsLoaders.put(Source.ACCUWATHER_API, () -> getLocationConfigs("accuweather_locations_api_config.csv"));
        locationsConfigsLoaders.put(Source.ALADIN_API, () -> getLocationConfigs("aladin_api_config.csv"));
        locationsConfigsLoaders.put(Source.CLEAR_OUTSIDE_WEB, () -> getLocationConfigs("clearoutside_web_config.csv"));
        locationsConfigsLoaders.put(Source.METEOBLUE_API, () -> getLocationConfigs("meteoblue_api_config.csv"));
        locationsConfigsLoaders.put(Source.METEOBLUE_WEB, () -> getLocationConfigs("meteoblue_web_config.csv"));
        locationsConfigsLoaders.put(Source.MET_NORWAY_API, () -> getLocationConfigs("met_norway_api_config.csv"));
        locationsConfigsLoaders.put(Source.OPEN_METEO_ECMWF, () -> getLocationConfigs("open_meteo_ecmwf_api_config.csv"));
    }

    public List<AccuWeatherLocationConfig> getAccuWeatherLocationConfigs() {
        return getAccuWeatherLocationConfigs("accuweather_api_config.csv");
    }

    public List<ChmuLocationConfig> getChmuLocationConfigs() {
        return getChmuLocationConfigs("chmu_measurements_web_config.csv");
    }

    public List<LocationIqLocationConfig> getLocationIqLocationConfigs() {
        return getLocationIqLocationConfigs("locationiq_reverse_geocoding_api_config.csv");
    }

    public List<LocationConfig> getLocationConfigs(Source source) {
        Supplier<List<LocationConfig>> supplier = locationsConfigsLoaders.get(source);
        if (supplier != null) {
            return supplier.get();
        } else {
            throw new IllegalArgumentException("No location configs for source = " + source);
        }
    }

    public Optional<LocationIqLocationConfig> getLocationIqLocationConfig(String locationName) {
        return getLocationIqLocationConfigs().stream().filter(c -> c.getName().equalsIgnoreCase(locationName)).findFirst();
    }

    public Optional<LocationConfig> getLocationConfig(Source source, String locationName) {
        Supplier<List<LocationConfig>> supplier = locationsConfigsLoaders.get(source);
        if (supplier != null) {
            return supplier.get().stream().filter(l -> l.getName().equals(locationName)).findFirst();
        } else {
            throw new IllegalArgumentException("No location config for source = '" + source + "' and location name = '" + locationName + "'");
        }
    }

    public Map<Integer, MeteobluePictorgramsConfig> getMeteobluePictogramsConfigs() {
        return CsvFile.fromResourceFile(getPath(meteobluePictogramsMappingFile)).getLines().stream()
                .map(l -> new MeteobluePictorgramsConfig(
                        l.getInt(PICTOGRAM_NO).orElseGet(new Fail<>(PICTOGRAM_NO)),
                        l.getString(PICTOGRAM_DESC).orElseGet(new Fail<>(PICTOGRAM_DESC)),
                        l.getInt(PICTOGRAM_CLOUD_COVERAGE).orElseGet(new Fail<>(PICTOGRAM_CLOUD_COVERAGE))
                )).collect(Collectors.toMap(MeteobluePictorgramsConfig::getPictogramId, m -> m));
    }

    List<Location> getLocations() {
        return CsvFile.fromResourceFile(getPath(locationsFile)).getLines().stream()
                .map(l -> new Location(
                        l.getString(LOCATION_NAME).orElseGet(new Fail<>(LOCATION_NAME)),
                        l.getDouble(LATITUDE).orElseGet(new Fail<>(LATITUDE)),
                        l.getDouble(LONGITUDE).orElseGet(new Fail<>(LONGITUDE))
                )).collect(Collectors.toList());
    }

    List<LocationConfig> getLocationConfigs(String configFile) {
        Map<String, Location> locationsByName = getLocationsByName();
        return CsvFile.fromResourceFile(getPath(configFile)).getLines().stream()
                .map(l -> new LocationConfig(
                        getLocation(locationsByName, l.getString(LOCATION_NAME).orElseGet(new Fail<>(LOCATION_NAME))),
                        l.getBoolean(ENABLED).orElseGet(new Fail<>(ENABLED)),
                        l.getTimes(SCRAPING_TIMES))
                ).collect(Collectors.toList());
    }

    List<AccuWeatherLocationConfig> getAccuWeatherLocationConfigs(String configFile) {
        Map<String, LocationConfig> configsByName = getLocationConfigsByName(configFile);
        return CsvFile.fromResourceFile(getPath(configFile)).getLines().stream()
                .map(l -> new AccuWeatherLocationConfig(
                        configsByName.get(l.getString(LOCATION_NAME).orElseGet(new Fail<>(LOCATION_NAME))),
                        l.getString(ACCUWEATHER_LOCATION_KEY).orElse(null)) // we might not always have this
                ).collect(Collectors.toList());
    }

    List<LocationIqLocationConfig> getLocationIqLocationConfigs(String configFile) {
        Map<String, LocationConfig> configsByName = getLocationConfigsByName(configFile);
        return CsvFile.fromResourceFile(getPath(configFile)).getLines().stream()
                .map(l -> new LocationIqLocationConfig(
                        configsByName.get(l.getString(LOCATION_NAME).orElseGet(new Fail<>(LOCATION_NAME))),
                        l.getString(LOCATIONIQ_COUNTY_NAME_OVERRIDE).orElse(null),
                        l.getString(LOCATIONIQ_REGION_NAME_OVERRIDE).orElse(null)
                )).collect(Collectors.toList());
    }

    List<ChmuLocationConfig> getChmuLocationConfigs(String configFile) {
        Map<String, Location> configsByName = getLocationsByName();
        return CsvFile.fromResourceFile(getPath(configFile)).getLines().stream()
                .map(l -> new ChmuLocationConfig(
                        configsByName.get(l.getString(LOCATION_NAME).orElseGet(new Fail<>(LOCATION_NAME))),
                        l.getString(CHMU_STATION).orElseGet(new Fail<>(CHMU_STATION))
                )).collect(Collectors.toList());
    }

    // ===== private methods =====

    private String getPath(String file) {
        return Path.of(sourceConfigFilesFolder, file).toString();
    }

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


    @Data
    private static class Fail<T> implements Supplier<T> {
        private final String header;
        @Override
        public T get() {
            throw new IllegalArgumentException("Failed to find value for header " + header);
        }
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

        static final String LOCATIONIQ_COUNTY_NAME_OVERRIDE = "CountyNameOverride";
        static final String LOCATIONIQ_REGION_NAME_OVERRIDE = "RegionNameOverride";
    }

}
