package com.google.weatherforecastchecker.scraper.forecast;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.weatherforecastchecker.LocationConfigRepository;
import com.google.weatherforecastchecker.Utils;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
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
@Profile("accuweather-locations")
public class AccuWeatherLocationsApiScraper {

    private final RestTemplate restTemplate;
    private final String urlTemplate;

    public AccuWeatherLocationsApiScraper(RestTemplate restTemplate,
                                          @Value("${accuweather.api.locations.url}") String urlTemplate) {
        this.restTemplate = restTemplate;
        this.urlTemplate = urlTemplate;
    }

    @PostConstruct
    public void scrapeLocations() {
        List<AccuWeatherLocationConfig> locationConfigs = LocationConfigRepository.getLocationConfigs(Source.ACCUWATHER_API);
        for (AccuWeatherLocationConfig locationConfig : locationConfigs) {
            scrapeLocationKey(locationConfig).ifPresent(dto -> {
                System.out.println("LOCATION=" + locationConfig.getName() + ",KEY=" + dto.getKey() + ",DETAILS=" + dto);
            });
        }
    }

    public Optional<LocationDto> scrapeLocationKey(AccuWeatherLocationConfig locationConfig) {
        if (locationConfig.getLocationKey() == null) {
            Map<String, Object> values = Map.of("lat", locationConfig.getLatitude(), "lon", locationConfig.getLongitude());
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
