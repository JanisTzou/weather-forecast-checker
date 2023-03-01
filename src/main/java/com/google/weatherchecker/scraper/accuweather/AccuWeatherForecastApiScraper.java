package com.google.weatherchecker.scraper.accuweather;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.weatherchecker.model.Source;
import com.google.weatherchecker.scraper.*;
import com.google.weatherchecker.model.Forecast;
import com.google.weatherchecker.scraper.ForecastScraper;
import com.google.weatherchecker.model.HourlyForecast;
import com.google.weatherchecker.util.Utils;
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
@Profile({"accuweather-locations", "default"})
public class AccuWeatherForecastApiScraper implements ForecastScraper<AccuWeatherLocationConfig> {

    private final RestTemplate restTemplate;
    private final AccuWeatherForecastApiScraperProps properties;
    private final LocationConfigRepository locationConfigRepository;

    public AccuWeatherForecastApiScraper(RestTemplate restTemplate,
                                         AccuWeatherForecastApiScraperProps properties,
                                         LocationConfigRepository locationConfigRepository) {
        this.restTemplate = restTemplate;
        this.properties = properties;
        this.locationConfigRepository = locationConfigRepository;
    }

    // TODO handle this org.springframework.web.client.HttpServerErrorException$ServiceUnavailable: 503 Unauthorized: "{"Code":"ServiceUnavailable","Message":"The allowed number of requests has been exceeded.","Reference":"/forecasts/v1/hourly/12hour/3395807?apikey=wBZYsN6UW85VptvHSO5L6UyOFyJ2K8gl&language=en&details=true&metric=true"}"

    @Override
    public Optional<Forecast> scrape(AccuWeatherLocationConfig location) {
        try {
            if (location.getLocationKey() != null) {
                Map<String, Object> values = Map.of("locationKey", location.getLocationKey());
                String url = Utils.fillTemplate(properties.getUrl(), values);
                ResponseEntity<HourForecastDto[]> resp = restTemplate.getForEntity(url, HourForecastDto[].class);
                if (resp.getBody() != null) {
                    List<HourlyForecast> forecasts = Arrays.stream(resp.getBody()).map(dto -> new HourlyForecast(dto.getDateTime(), dto.getCloudCover(), null)).collect(Collectors.toList());
                    return Optional.of(new Forecast(LocalDateTime.now(), getSource(), location.getLocation(), forecasts));
                }
                AccuWeatherApiUtils.logRemainingRateLimit(resp);
            }
        } catch (Exception e) {
            log.error("Failed to scrape page ", e);
        }
        return Optional.empty();
    }

    @Override
    public List<AccuWeatherLocationConfig> getLocationConfigs() {
        return locationConfigRepository.getAccuWeatherLocationConfigs();
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
