package com.google.weatherforecastchecker.scraper.forecast;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.weatherforecastchecker.scraper.ForecastScrapingProps;
import com.google.weatherforecastchecker.scraper.Source;
import com.google.weatherforecastchecker.util.Utils;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Log4j2
@Component
@Profile({"accuweather", "default"})
public class AccuWeatherApiScraper implements ForecastScraper<AccuWeatherLocationConfig> {

    private final RestTemplate restTemplate;
    private final AccuWeatherApiScraperProps properties;

    public AccuWeatherApiScraper(RestTemplate restTemplate,
                                 AccuWeatherApiScraperProps properties) {
        this.restTemplate = restTemplate;
        this.properties = properties;
    }

    @Override
    public Optional<Forecast> scrape(AccuWeatherLocationConfig locationConfig) {
        try {
            if (locationConfig.getLocationKey() != null) {
                Map<String, Object> values = Map.of("locationKey", locationConfig.getLocationKey());
                String url = Utils.fillTemplate(properties.getUrl(), values);
                log.info(url);
                ResponseEntity<HourForecastDto[]> resp = restTemplate.getForEntity(url, HourForecastDto[].class);
                if (resp.getBody() != null) {
                    List<HourForecast> forecasts = Arrays.stream(resp.getBody()).map(dto -> new HourForecast(dto.getDateTime(), dto.getCloudCover(), null)).collect(Collectors.toList());
                    return Optional.of(new Forecast(getSource(), locationConfig.getName(), forecasts));
                }
            }
        } catch (Exception e) {
            log.error("Failed to scrape page ", e);
        }
        return Optional.empty();
    }

    @Override
    public Source getSource() {
        return Source.ACCUWATHER_API;
    }

    @Override
    public ForecastScrapingProps getScrapingProps() {
        return properties;
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
