package com.google.weatherforecastchecker.scraper.forcast;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.google.weatherforecastchecker.Utils;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.time.LocalDateTime;
import java.util.*;

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

    @PostConstruct
    public void init() {
        scrape(new AccuWeatherLocation("123456"));
    }

    @Override
    public List<DayForecast> scrape(List<AccuWeatherLocation> locations) {
        return null;
    }

    @Override
    public List<DayForecast> scrape(AccuWeatherLocation location) {
        try {
            if (location.getLocationKey() != null) {
                Map<String, Object> values = Map.of("locationKey", location.getLocationKey());
                String url = Utils.fillTemplate(urlTemplate, values);
                log.info(url);
//                ResponseEntity<Forecast> forecast = restTemplate.getForEntity(url, Forecast.class);

                CollectionType javaType = jsonMapper.getTypeFactory().constructCollectionType(List.class, HourForecastDto.class);
                List<HourForecastDto> forecasts = jsonMapper.readValue(new File("/Users/janis/IdeaProjects/weather-forecast-checker/docs/accu-weather/response_example_with_metrics_true_details_true.json"), javaType);

                System.out.println(forecasts);
            }
        } catch (Exception e) {
            log.error("Failed to scrape page ", e);
        }
        return Collections.emptyList();
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
