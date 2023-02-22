package com.google.weatherforecastchecker.scraper.forcast;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.weatherforecastchecker.LocationsReader;
import com.google.weatherforecastchecker.Location;
import com.google.weatherforecastchecker.Utils;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Log4j2
@Component
public class AladinScraper implements ForecastScraper<Location> {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // https://stackoverflow.com/questions/51958805/spring-boot-mvc-resttemplate-where-to-initialize-a-resttemplate-for-a-mvc-a
    private final RestTemplate restTemplate;
    private final String urlTemplate;

    public AladinScraper(RestTemplate jsonAsTextRestTemplate,
                         @Value("${aladin.api.forecast.url.template}") String urlTemplate) {
        this.restTemplate = jsonAsTextRestTemplate;
        this.urlTemplate = urlTemplate;
    }

//    @PostConstruct
    public void scrape() {
        List<Location> locations = LocationsReader.getLocationConfigs();
        scrape(locations);
    }

    @Override
    public List<DayForecast> scrape(List<Location> locations) {
        return locations.stream()
                .flatMap(l -> scrape(l).stream())
                .peek(f -> {
                    System.out.println(f);
                    Utils.sleep(3000);
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<DayForecast> scrape(Location location) {
        Map<String, Object> values = Map.of("lat", location.getLatitude(), "lon", location.getLongitude());
        String url = Utils.fillTemplate(urlTemplate, values);
        log.info(url);
        ResponseEntity<AladinForcast> forecast = restTemplate.getForEntity(url, AladinForcast.class); // content is text/content -> parse manually ...
        return toDayForecast(location.getLocationName(), forecast.getBody());
    }

    public List<DayForecast> toDayForecast(String location, AladinForcast af) {
        LocalDateTime dateTime = LocalDateTime.parse(af.forecastTimeIso, DATE_TIME_FORMATTER);
        List<Double> cloudsTotal = af.getParameterValues().getCloudsTotal();
        List<DayForecast> forecasts = new ArrayList<>();
        LocalDateTime coveragesStart = dateTime;
        List<Integer> coveragesForDay = new ArrayList<>();


        for (int hour = 0; hour < cloudsTotal.size(); hour++) {
            LocalDateTime nextHour = dateTime.plusHours(hour);
            boolean isLast = hour == cloudsTotal.size() - 1;
            int coverage = (int) (cloudsTotal.get(hour) * 100);
            boolean newDay = nextHour.toLocalTime().equals(LocalTime.MIDNIGHT);
            if (newDay) {
                DayForecast f = new DayForecast(location, coveragesStart, coveragesForDay, Collections.emptyList());
                forecasts.add(f);
                coveragesForDay = new ArrayList<>();
                coveragesForDay.add(coverage);
                coveragesStart = nextHour;
            } else {
                coveragesForDay.add(coverage);
                if (isLast) {
                    DayForecast f = new DayForecast(location, coveragesStart, coveragesForDay, Collections.emptyList());
                    forecasts.add(f);
                }
            }
        }
        return forecasts;
    }

    @Data
    @NoArgsConstructor
    public static final class AladinForcast {
        private String forecastTimeIso;
        private ParameterValues parameterValues;
    }

    @Data
    @NoArgsConstructor
    public static final class ParameterValues {
        @JsonProperty("CLOUDS_TOTAL")
        private List<Double> cloudsTotal;
    }


}
