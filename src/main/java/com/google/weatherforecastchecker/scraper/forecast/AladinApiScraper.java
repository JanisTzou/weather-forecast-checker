package com.google.weatherforecastchecker.scraper.forecast;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.weatherforecastchecker.LocationConfig;
import com.google.weatherforecastchecker.LocationConfigRepository;
import com.google.weatherforecastchecker.Utils;
import jakarta.annotation.Nullable;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.context.properties.ConfigurationProperties;
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
@Profile("aladin")
public class AladinApiScraper implements ForecastScraper<LocationConfig> {

    // https://stackoverflow.com/questions/51958805/spring-boot-mvc-resttemplate-where-to-initialize-a-resttemplate-for-a-mvc-a
    private final RestTemplate restTemplate;
    private final Properties properties;

    public AladinApiScraper(RestTemplate jsonAsTextRestTemplate,
                            Properties properties) {
        this.restTemplate = jsonAsTextRestTemplate;
        this.properties = properties;
    }

    @PostConstruct
    public void scrape() {
        List<LocationConfig> locationConfigs = LocationConfigRepository.getLocationConfigs(getSource());
        scrape(locationConfigs);
    }

    @Override
    public Source getSource() {
        return Source.ALADIN_API;
    }

    @Override
    public ScrapingProperties getScrapingProperties() {
        return properties;
    }

    @Override
    public Optional<Forecast> scrape(LocationConfig location) {
        Map<String, Object> values = Map.of("lat", location.getLatitude(), "lon", location.getLongitude());
        String url = Utils.fillTemplate(properties.getUrl(), values);
//        log.info(url);
        ResponseEntity<ForecastDto> resp = restTemplate.getForEntity(url, ForecastDto.class); // content is text/content -> parse manually ...
        return toForecast(location.getName(), resp.getBody());
    }

    public Optional<Forecast> toForecast(String location, @Nullable ForecastDto forecastDto) {
        if (forecastDto == null) {
            return Optional.empty();
        }

        LocalDateTime dateTime = forecastDto.getForecastTimeIso();
        List<Double> hourCloudCoverages = forecastDto.getParameterValues().getCloudsTotal();

        List<HourForecast> forecasts = IntStream.rangeClosed(0, hourCloudCoverages.size() - 1)
                .mapToObj(hourNo -> {
                    LocalDateTime hour = dateTime.plusHours(hourNo);
                    int coverage = (int) (hourCloudCoverages.get(hourNo) * 100);
                    return new HourForecast(hour, coverage, null);
                })
                .collect(Collectors.toList());

        return Optional.of(new Forecast(getSource(), location, forecasts));
    }



    @Data
    @NoArgsConstructor
    public static final class ForecastDto {
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime forecastTimeIso;
        private ParameterValues parameterValues;
    }

    @Data
    @NoArgsConstructor
    public static final class ParameterValues {
        @JsonProperty("CLOUDS_TOTAL")
        private List<Double> cloudsTotal;
    }

    @ConfigurationProperties("aladin.api.forecast")
    public static class Properties extends ScrapingProperties {
    }

}
