package com.google.weatherforecastchecker.scraper.forecast;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.google.weatherforecastchecker.Utils;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Log4j2
@Component
public class AccuWeatherScraper implements ForecastScraper<AccuWeatherLocation> {

    private final RestTemplate restTemplate;
    private final String urlTemplate;
    private final JsonMapper jsonMapper;

    public AccuWeatherScraper(RestTemplate restTemplate,
                              @Value("${accuweather.api.forecast.url.template}") String urlTemplate,
                              JsonMapper jsonMapper) {
        this.restTemplate = restTemplate;
        this.urlTemplate = urlTemplate;
        this.jsonMapper = jsonMapper;
    }

//    @PostConstruct
    public void init() {
        // TODO
    }

    @Override
    public List<Forecast> scrape(List<AccuWeatherLocation> locations) {
        return null;
    }

    @Override
    public Optional<Forecast> scrape(AccuWeatherLocation location) {
        try {
            if (location.getLocationKey() != null) {
                Map<String, Object> values = Map.of("locationKey", location.getLocationKey());
                String url = Utils.fillTemplate(urlTemplate, values);
                log.info(url);
                ResponseEntity<HourForecastDto[]> resp = restTemplate.getForEntity(url, HourForecastDto[].class);
                if (resp.getBody() != null) {
                    List<HourForecast> forecasts = Arrays.stream(resp.getBody()).map(dto -> new HourForecast(dto.getDateTime(), dto.getCloudCover(), null)).collect(Collectors.toList());
                    return Optional.of(new Forecast(location.getLocationName(), forecasts));
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

}
