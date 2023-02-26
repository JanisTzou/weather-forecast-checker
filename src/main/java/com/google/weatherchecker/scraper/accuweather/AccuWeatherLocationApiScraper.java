package com.google.weatherchecker.scraper.accuweather;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.weatherchecker.model.Location;
import com.google.weatherchecker.model.Source;
import com.google.weatherchecker.scraper.*;
import com.google.weatherchecker.scraper.LocationBasedScraper;
import com.google.weatherchecker.util.Utils;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Used only ad-hoc for scraping locations for which we do not have AccuWeather's locationKey
 */
@Log4j2
@Component
@Profile({"accuweather-locations", "default"})
public class AccuWeatherLocationApiScraper implements LocationBasedScraper<LocationConfig, AccuWeatherLocationKey> {

    private final RestTemplate restTemplate;
    private final AccuWeatherLocationApiScraperProps props;
    private final LocationConfigRepository locationConfigRepository;

    public AccuWeatherLocationApiScraper(RestTemplate restTemplate,
                                         LocationConfigRepository locationConfigRepository,
                                         AccuWeatherLocationApiScraperProps props) {
        this.restTemplate = restTemplate;
        this.props = props;
        this.locationConfigRepository = locationConfigRepository;
    }

    @Override
    public Optional<AccuWeatherLocationKey> scrape(LocationConfig location) {
        Map<String, Object> values = Map.of("lat", location.getLatitude(), "lon", location.getLongitude());
        String url = Utils.fillTemplate(props.getUrl(), values);
        log.info(url);
        ResponseEntity<LocationDto> resp = restTemplate.getForEntity(url, LocationDto.class);
        AccuWeatherApiUtils.logRemainingRateLimit(resp);
        if (resp.getBody() != null) {
            return Optional.of(map(location.getLocation(), resp.getBody()));
        }
        return Optional.empty();
    }


    @Override
    public AccuWeatherLocationApiScraperProps getScrapingProps() {
        return props;
    }

    @Override
    public Source getSource() {
        return Source.ACCUWATHER_API;
    }

    @Override
    public List<LocationConfig> getLocationConfigs() {
        return locationConfigRepository.getLocationConfigs(getSource());
    }

    private AccuWeatherLocationKey map(Location location, LocationDto locationDto) {
        return new AccuWeatherLocationKey(location, locationDto.getKey());
    }

    @Data
    @NoArgsConstructor
    public static class LocationDto {
        @JsonProperty("Key")
        private String key;

        @JsonProperty("LocalizedName")
        private String localizedName;

        @JsonProperty("AdministrativeArea")
        private AdministrativeAreaDto administrativeArea;
    }

    @Data
    @NoArgsConstructor
    public static class AdministrativeAreaDto {
        @JsonProperty("LocalizedName")
        private String localizedName;
    }

}
