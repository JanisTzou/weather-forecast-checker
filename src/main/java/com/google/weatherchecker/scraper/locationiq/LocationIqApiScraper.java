package com.google.weatherchecker.scraper.locationiq;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.weatherchecker.model.Location;
import com.google.weatherchecker.model.Source;
import com.google.weatherchecker.scraper.*;
import com.google.weatherchecker.util.Utils;
import jakarta.annotation.Nullable;
import jakarta.annotation.PostConstruct;
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

@Log4j2
@Component
@Profile({"locationiq", "default"})
public class LocationIqApiScraper implements LocationBasedScraper<LocationIqLocationConfig, Location> {

    private final RestTemplate restTemplate;
    private final LocationIqApiScraperProps properties;
    private final LocationConfigRepository locationConfigRepository;

    public LocationIqApiScraper(RestTemplate jsonAsTextRestTemplate,
                                LocationIqApiScraperProps properties,
                                LocationConfigRepository locationConfigRepository) {
        this.restTemplate = jsonAsTextRestTemplate;
        this.properties = properties;
        this.locationConfigRepository = locationConfigRepository;
    }

    @Override
    public Source getSource() {
        return Source.LOCATION_IQ_API;
    }

    @Override
    public LocationScrapingProps getScrapingProps() {
        return properties;
    }

    @Override
    public Optional<Location> scrape(LocationIqLocationConfig location) {
        Map<String, Object> values = Map.of("lat", location.getLatitude(), "lon", location.getLongitude());
        String url = Utils.fillTemplate(properties.getUrl(), values);
        ResponseEntity<LocationDto> resp = restTemplate.getForEntity(url, LocationDto.class); // content is text/content -> parse manually ...
        return toEnrichedLocation(location.getLocation(), resp.getBody());
    }

    @Override
    public List<LocationIqLocationConfig> getLocationConfigs() {
        return locationConfigRepository.getLocationIqLocationConfigs();
    }

    public Optional<Location> toEnrichedLocation(Location location, @Nullable LocationDto locationDto) {
        if (locationDto != null && locationDto.getAddress() != null) {
            Optional<String> countyNameOverride = locationConfigRepository.getLocationIqLocationConfig(location.getName()).flatMap(LocationIqLocationConfig::getCountyNameOverride);
            Optional<String> regionNameOverride = locationConfigRepository.getLocationIqLocationConfig(location.getName()).flatMap(LocationIqLocationConfig::getRegionNameOverride);

            AddressDto address = locationDto.getAddress();
            return Optional.of(new Location(
                    location.getName(),
                    location.getLatitude(),
                    location.getLongitude(),
                    address.getMunicipality(),
                    countyNameOverride.orElse(address.getCounty()),
                    regionNameOverride.orElse(address.region),
                    true
            ));
        }
       return Optional.empty();
    }


    @Data
    @NoArgsConstructor
    public static class LocationDto {
        @JsonProperty("address")
        private AddressDto address;
    }

    @Data
    @NoArgsConstructor
    public static class AddressDto {

        @JsonProperty("state")
        private String region;

        @JsonProperty("county")
        private String county;

        @JsonProperty("municipality")
        private String municipality;
    }

}
