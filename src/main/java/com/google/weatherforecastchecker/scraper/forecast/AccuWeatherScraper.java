package com.google.weatherforecastchecker.scraper.forecast;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.weatherforecastchecker.LocationsReader;
import com.google.weatherforecastchecker.Utils;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Log4j2
@Component
@Profile("accuweather")
public class AccuWeatherScraper implements ForecastScraper<AccuWeatherLocation> {

    private final RestTemplate restTemplate;
    private final Properties properties;

    public AccuWeatherScraper(RestTemplate restTemplate,
                              Properties properties) {
        this.restTemplate = restTemplate;
        this.properties = properties;
    }

//    @PostConstruct
    public void init() {
        scrape(LocationsReader.getAccuWeatherLocations());
    }

    @Override
    public List<Forecast> scrape(List<AccuWeatherLocation> locations) {
        return locations.stream()
                .flatMap(l -> scrape(l).stream())
                .peek(f -> {
                    System.out.println(f);
                    Utils.sleep(properties.getDelayBetweenRequests().toMillis());
                })
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Forecast> scrape(AccuWeatherLocation location) {
        try {
            if (location.getLocationKey() != null) {
                Map<String, Object> values = Map.of("locationKey", location.getLocationKey());
                String url = Utils.fillTemplate(properties.getUrl(), values);
                log.info(url);
                ResponseEntity<HourForecastDto[]> resp = restTemplate.getForEntity(url, HourForecastDto[].class);
                if (resp.getBody() != null) {
                    List<HourForecast> forecasts = Arrays.stream(resp.getBody()).map(dto -> new HourForecast(dto.getDateTime(), dto.getCloudCover(), null)).collect(Collectors.toList());
                    return Optional.of(new Forecast(Source.ACCUWATHER, location.getLocationName(), forecasts));
                }
            }
        } catch (Exception e) {
            log.error("Failed to scrape page ", e);
        }
        return Optional.empty();
    }


    @Data
    @NoArgsConstructor
    public static class HourForecastDto {
        @JsonProperty("DateTime")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
        private LocalDateTime dateTime;

        @JsonProperty("CloudCover")
        private int cloudCover;
    }

    @ConfigurationProperties("accuweather.api.forecast")
    public static class Properties extends ScrapingProperties {
    }

}
