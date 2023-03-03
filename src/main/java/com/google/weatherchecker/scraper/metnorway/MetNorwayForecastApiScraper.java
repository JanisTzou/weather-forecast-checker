package com.google.weatherchecker.scraper.metnorway;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.weatherchecker.model.Forecast;
import com.google.weatherchecker.model.HourlyForecast;
import com.google.weatherchecker.model.Location;
import com.google.weatherchecker.model.Source;
import com.google.weatherchecker.scraper.ForecastScraper;
import com.google.weatherchecker.scraper.ForecastScrapingProps;
import com.google.weatherchecker.scraper.LocationConfig;
import com.google.weatherchecker.scraper.LocationConfigRepository;
import com.google.weatherchecker.util.Utils;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Profile;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Log4j2
@Component
@Profile({"met-norway", "default"})
public class MetNorwayForecastApiScraper implements ForecastScraper<LocationConfig> {

    private final RestTemplate restTemplate;
    private final MetNorwayForecastApiScraperProps props;
    private final LocationConfigRepository locationConfigRepository;

    public MetNorwayForecastApiScraper(RestTemplate restTemplate,
                                       MetNorwayForecastApiScraperProps props,
                                       LocationConfigRepository locationConfigRepository) {
        this.restTemplate = restTemplate;
        this.props = props;
        this.locationConfigRepository = locationConfigRepository;
    }

    @Override
    public Optional<Forecast> scrape(LocationConfig location) {
        try {
            Map<String, Object> values = Map.of("lat", location.getLatitude(), "lon", location.getLongitude());
            String url = Utils.fillTemplate(props.getUrl(), values);

            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(List.of(MediaType.APPLICATION_JSON));
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("User-Agent", "github.com/JanisTzou/weather-forecast-checker tzoumas.janis@gmail.com"); // TODO put to env.properties ...
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<ForecastDto> resp = restTemplate.exchange(url, HttpMethod.GET, entity, ForecastDto.class);
            return mapToForecast(resp.getBody(), location.getLocation());
        } catch (Exception e) {
            log.error("Failed to scrape page ", e);
        }
        return Optional.empty();
    }

    @Override
    public List<LocationConfig> getLocationConfigs() {
        return locationConfigRepository.getLocationConfigs(getSource());
    }

    @Override
    public Source getSource() {
        return Source.MET_NORWAY_API;
    }

    @Override
    public ForecastScrapingProps getScrapingProps() {
        return props;
    }

    private Optional<Forecast> mapToForecast(ForecastDto forecastDto, Location location) {
        if (forecastDto != null) {
            List<HourlyForecast> hourlyForecasts = mapToHourForcasts(forecastDto);
            // TODO check if empty ...
            // TODO add updated time ...
            return Optional.of(new Forecast(LocalDateTime.now(), getSource(), location, hourlyForecasts));
        }
        return Optional.empty();
    }

    private List<HourlyForecast> mapToHourForcasts(ForecastDto forecastDto) {
        Properties properties = forecastDto.properties;
        if (properties != null) {
            List<TimeSeries> timeseries = properties.timeseries;
            if (timeseries != null) {
                int maxHoursToInclude = props.getDays() * 24;
                return timeseries.stream()
                        .limit(maxHoursToInclude) // TODO make sure that we are going by hour intervals actually ... and leave out anything else ...
                        .map(s -> {
                            LocalDateTime time = s.getTime();
                            Optional<Details> details = s.getDetails();
                            if (time != null && details.isPresent()) {
                                // TODO convert property to CET ! this is temporary ...
                                return new HourlyForecast(time.plusHours(1L), (int) details.get().getTotalCloud(), "");
                            } else {
                                return null;
                            }
                        })
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());
            }
        }
        return Collections.emptyList();
    }


    @Data
    @NoArgsConstructor
    public static class ForecastDto {
        @JsonProperty("properties")
        private Properties properties;
    }

    @Data
    @NoArgsConstructor
    public static class Properties {
        @JsonProperty("meta")
        private Meta meta;

        @JsonProperty("timeseries")
        private List<TimeSeries> timeseries;
    }

    @Data
    @NoArgsConstructor
    public static class Meta {
        @JsonProperty("updated_at")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssVV")
        private LocalDateTime updatedAt;
//        private String updatedAt;
    }


    @Data
    @NoArgsConstructor
    public static class TimeSeries {

        @JsonProperty("time")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssVV")
        private LocalDateTime time;
//        private String time;

        @JsonProperty("data")
        private HourData data;

        Optional<Details> getDetails() {
            if (data != null) {
                if (data.getInstant() != null) {
                    return Optional.ofNullable(data.getInstant().getDetails());
                }
            }
            return Optional.empty();
        }
    }

    @Data
    @NoArgsConstructor
    public static class HourData {
        @JsonProperty("instant")
        private Instant instant;
    }

    @Data
    @NoArgsConstructor
    public static class Instant {
        @JsonProperty("details")
        private Details details;


    }

    @Data
    @NoArgsConstructor
    public static class Details {
        @JsonProperty("cloud_area_fraction")
        private double totalCloud;
    }

}
