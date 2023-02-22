package com.google.weatherforecastchecker.scraper.forecast;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.google.weatherforecastchecker.LocationsReader;
import com.google.weatherforecastchecker.Utils;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
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
public class AccuWeatherLocationsScraper {

    private final RestTemplate restTemplate;
    private final String urlTemplate;
    private final JsonMapper jsonMapper;

    public AccuWeatherLocationsScraper(RestTemplate restTemplate,
                                       @Value("${accuweather.api.locatons.url.template}") String urlTemplate,
                                       JsonMapper jsonMapper) {
        this.restTemplate = restTemplate;
        this.urlTemplate = urlTemplate;
        this.jsonMapper = jsonMapper;
    }

    public void scrapeLocations() {
        for (AccuWeatherLocation location : LocationsReader.getAccuWeatherLocations()) {
            scrapeLocationKey(location).ifPresent(dto -> {
                System.out.println("LOCATION=" + location.getLocationName() + ",KEY=" + dto.getKey() + ",DETAILS=" + dto);
            });
        }
    }

    public Optional<LocationDto> scrapeLocationKey(AccuWeatherLocation location) {
        if (location.getLocationKey() == null) {
            Map<String, Object> values = Map.of("lat", location.getLatitude(), "lon", location.getLongitude());
            String url = Utils.fillTemplate(urlTemplate, values);
            log.info(url);
            ResponseEntity<LocationDto> resp = restTemplate.getForEntity(url, LocationDto.class);
            logRemainingRateLimit(resp);
            return Optional.ofNullable(resp.getBody());
        }
        return Optional.empty();
    }

    private void logRemainingRateLimit(ResponseEntity<LocationDto> resp) {
        List<String> headers = resp.getHeaders().get("RateLimit-Remaining");
        if (headers != null) {
            Optional<String> remainingRateLimit = headers.stream().findFirst();
            remainingRateLimit.ifPresent(s -> log.info("Remaining rate limit = {}", s));
        }
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
