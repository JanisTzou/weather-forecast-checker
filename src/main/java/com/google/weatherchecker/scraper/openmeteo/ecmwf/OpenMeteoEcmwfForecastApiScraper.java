package com.google.weatherchecker.scraper.openmeteo.ecmwf;

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
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Log4j2
@Component
@Profile({"open-meteo-ecmwf", "default"})
public class OpenMeteoEcmwfForecastApiScraper implements ForecastScraper<LocationConfig> {

    private final RestTemplate restTemplate;
    private final OpenMeteoEcmwfForecastApiScraperProps props;
    private final LocationConfigRepository locationConfigRepository;

    public OpenMeteoEcmwfForecastApiScraper(RestTemplate restTemplate,
                                            OpenMeteoEcmwfForecastApiScraperProps props,
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
            log.info(url);
            ResponseEntity<ForecastDto> resp = restTemplate.getForEntity(url, ForecastDto.class);
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
        return Source.OPEN_METEO_ECMWF;
    }

    @Override
    public ForecastScrapingProps getScrapingProps() {
        return props;
    }

    private Optional<Forecast> mapToForecast(ForecastDto forecastDto, Location location) {
        if (forecastDto != null) {
            List<HourlyForecast> hourlyForecasts = mapToHourForecasts(forecastDto);
            return Optional.of(new Forecast(LocalDateTime.now(), getSource(), location, hourlyForecasts));
        }
        return Optional.empty();
    }

    private List<HourlyForecast> mapToHourForecasts(ForecastDto forecastDto) {
        Hourly hourly = forecastDto.getHourly();
        int count = Math.min(hourly.getTime().size(), hourly.getTotalCloudCover().size());
        int maxThreeHourIntervalsToInclude = props.getDays() * 7;
        int threeHoursCount = Math.min(count, maxThreeHourIntervalsToInclude);
        return IntStream.range(0, threeHoursCount)
                .mapToObj(h -> mapToOneHourIntervals(hourly, h)
                )
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    // TODO decide if spreading the 3 hour intervals forewrds is corect or not ...
    private List<HourlyForecast> mapToOneHourIntervals(Hourly hourly, int h) {
        return IntStream.range(0, 3)
                .mapToObj(incr -> {
                    return new HourlyForecast(
                            hourly.getTime().get(h).plusHours(incr + 1), // TODO +1 temporary conversion GMT to CEST
                            hourly.getTotalCloudCover().get(h),
                            null
                    );
                })
                .collect(Collectors.toList());
    }

    @Data
    @NoArgsConstructor
    public static class ForecastDto {
        @JsonProperty("timezone_abbreviation")
        private String timezoneAbbreviation;

        @JsonProperty("hourly")
        private Hourly hourly;
    }

    @Data
    @NoArgsConstructor
    public static class Hourly {
        @JsonProperty("time")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm")
        private List<LocalDateTime> time;

        @JsonProperty("cloudcover")
        private List<Integer> totalCloudCover;
    }

}
