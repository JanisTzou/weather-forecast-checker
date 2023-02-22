package com.google.weatherforecastchecker.scraper.forecast;

import com.gargoylesoftware.htmlunit.html.*;
import com.google.weatherforecastchecker.LocationsReader;
import com.google.weatherforecastchecker.Utils;
import com.google.weatherforecastchecker.htmlunit.HtmlUnitClientFactory;
import com.google.weatherforecastchecker.Location;
import jakarta.annotation.PostConstruct;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.google.weatherforecastchecker.htmlunit.HtmlUnitUtils.*;

@Log4j2
@Component
@Profile("clearoutside")
public class ClearOutsideScraper implements ForecastScraper<Location> {

    private final String urlTemplate;
    private final int daysToScrape;

    public ClearOutsideScraper(@Value("${clearoutside.web.forecast.url.template}") String urlTemplate,
                               @Value("${meteoblue.web.forecast.days}") int days) {
        this.urlTemplate = urlTemplate;
        this.daysToScrape = days;
    }

    @PostConstruct
    public void scrape() {
        List<Location> locations = LocationsReader.getLocations();
        scrape(locations);
    }

    @Override
    public List<Forecast> scrape(List<Location> locations) {
        return LocationsReader.getLocations().stream()
                .flatMap(l -> scrape(l).stream())
                .peek(f -> {
                    System.out.println(f);
                    Utils.sleep(60_000);
                })
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Forecast> scrape(Location location) {
        try {
            Map<String, Object> values = Map.of("lat", location.getLatitude(), "lon", location.getLongitude());
            String url = Utils.fillTemplate(urlTemplate, values);

            HtmlPage page = HtmlUnitClientFactory.startDriver().getPage(url);
            DomElement forecastEl = page.getElementById("forecast");

            List<HourForecast> hourForecasts = new ArrayList<>();

            int count = 0;
            for (DomElement day : forecastEl.getChildElements()) {
                count++;
                if (count <= daysToScrape) {
                    Optional<LocalDate> date = getHtmlElementDescendants(day, d -> hasCssClass(d, "fc_day_date")).stream()
                            .findFirst().map(d -> d.getTextContent().trim()).flatMap(d -> Utils.getFirstMatch(d, "\\d+"))
                            .map(Integer::parseInt)
                            .flatMap(this::gateDate);

                    Optional<HtmlElement> totalCloudHourlyEl = getHtmlElementDescendants(day, d -> hasCssClass(d, "fc_detail_row")).stream().map(d -> (HtmlElement) d).findFirst();

                    // expected 24 values ... TODO validation
                    List<Integer> hourCloudCoverages = totalCloudHourlyEl.stream().flatMap(e -> e.getElementsByTagName("li").stream())
                            .map(e -> Integer.parseInt(e.getTextContent().trim()))
                            .collect(Collectors.toList());

                    LocalDateTime dateTime = LocalDateTime.of(date.get(), LocalTime.MIDNIGHT);

                    List<HourForecast> hourForcastsForDay = IntStream.rangeClosed(0, hourCloudCoverages.size() - 1)
                            .mapToObj(hourNo -> new HourForecast(dateTime.plusHours(hourNo), hourCloudCoverages.get(hourNo), null))
                            .collect(Collectors.toList());

                    hourForecasts.addAll(hourForcastsForDay);
                }
            }

            return Optional.of(new Forecast(Source.CLEAR_OUTSIDE, location.getLocationName(), hourForecasts));

        } catch (Exception e) {
            log.error("Failed to scrape page ", e);
        }

        return Optional.empty();
    }

    private Optional<LocalDate> gateDate(int dayOfMonth) {
        return Optional.ofNullable(getDatesByDay().get(dayOfMonth));
    }

    private Map<Integer, LocalDate> getDatesByDay() {
        Map<Integer, LocalDate> daysMap = new HashMap<>();
        for (int incr = -1; incr < 10; incr++) {
            LocalDate date = LocalDate.now().plusDays(incr);
            daysMap.put(date.getDayOfMonth(), date);
        }
        return daysMap;
    }


}
