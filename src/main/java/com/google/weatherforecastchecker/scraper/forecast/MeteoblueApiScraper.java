package com.google.weatherforecastchecker.scraper.forecast;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.weatherforecastchecker.scraper.ForecastScrapingProps;
import com.google.weatherforecastchecker.scraper.LocationConfig;
import com.google.weatherforecastchecker.scraper.LocationConfigRepository;
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
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Log4j2
@Component
@Profile({"meteoblue-api", "default"})
public class MeteoblueApiScraper implements ForecastScraper<LocationConfig> {

    private final RestTemplate restTemplate;
    private final MeteoblueApiScraperProps props;
    private final LocationConfigRepository locationConfigRepository;

    public MeteoblueApiScraper(RestTemplate restTemplate,
                               MeteoblueApiScraperProps props,
                               LocationConfigRepository locationConfigRepository) {
        this.restTemplate = restTemplate;
        this.props = props;
        this.locationConfigRepository = locationConfigRepository;
    }

    @Override
    public Optional<Forecast> scrape(LocationConfig locationConfig) {
        try {
            Map<String, Object> values = Map.of("lat", locationConfig.getLatitude(), "lon", locationConfig.getLongitude());
            String url = Utils.fillTemplate(props.getUrl(), values);
            log.info(url);
            ResponseEntity<ForecastDto> resp = restTemplate.getForEntity(url, ForecastDto.class);
            return mapToForecast(resp.getBody(), locationConfig.getName());
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
        return Source.METEOBLUE_API;
    }

    @Override
    public ForecastScrapingProps getScrapingProps() {
        return props;
    }

    private Optional<Forecast> mapToForecast(ForecastDto forecastDto, String locationName) {
        if (forecastDto != null) {
            List<HourForecast> hourForecasts = mapToHourForcasts(forecastDto);
            return Optional.of(new Forecast(getSource(), locationName, hourForecasts));
        }
        return Optional.empty();
    }

    private List<HourForecast> mapToHourForcasts(ForecastDto forecastDto) {
        Data1Hr data1Hr = forecastDto.getData1Hr();
        int count = Math.min(data1Hr.getTime().size(), data1Hr.getTotalCloudCover().size());
        int maxHoursToInclude = props.getDays() * 24;
        int hours = Math.min(count, maxHoursToInclude);
        List<HourForecast> hourForecasts = IntStream.range(1, hours)
                .mapToObj(h -> new HourForecast(
                                data1Hr.getTime().get(h),
                                data1Hr.getTotalCloudCover().get(h),
                                null
                        )
                )
                .collect(Collectors.toList());
        return hourForecasts;
    }

    @Data
    @NoArgsConstructor
    public static class ForecastDto {
        @JsonProperty("metadata")
        private Metadata metadata;

        @JsonProperty("data_1h")
        private Data1Hr data1Hr;
    }

    @Data
    @NoArgsConstructor
    public static class Data1Hr {
        @JsonProperty("time")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm")
        private List<LocalDateTime> time;

        @JsonProperty("totalcloudcover")
        private List<Integer> totalCloudCover;
    }

    @Data
    @NoArgsConstructor
    public static class Metadata {
        @JsonProperty("modelrun_utc")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm")
        private LocalDateTime modelRunUtc;

        @JsonProperty("modelrun_updatetime_utc")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm")
        private LocalDateTime modelRunUpdateTimeUtc;
    }

}
