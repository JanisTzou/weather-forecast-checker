package com.google.weatherforecastchecker.scraper.forcast;

import com.gargoylesoftware.htmlunit.html.*;
import com.google.weatherforecastchecker.LocationsReader;
import com.google.weatherforecastchecker.Utils;
import com.google.weatherforecastchecker.htmlunit.HtmlUnitClientFactory;
import com.google.weatherforecastchecker.Location;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.google.weatherforecastchecker.htmlunit.HtmlUnitUtils.*;

@Log4j2
@Component
public class ClearOutsideScraper implements ForecastScraper<Location> {

    private final String urlTemplate;

    public ClearOutsideScraper(@Value("${clearoutside.web.forecast.url.template}") String urlTemplate) {
        this.urlTemplate = urlTemplate;
    }

    @Override
    public List<DayForecast> scrape(List<Location> locations) {
        return LocationsReader.getLocationConfigs().stream()
                .flatMap(l -> scrape(l).stream())
                .peek(f -> {
                    System.out.println(f);
                    Utils.sleep(60_000);
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<DayForecast> scrape(Location location) {
        try {
            Map<String, Object> values = Map.of("lat", location.getLatitude(), "lon", location.getLongitude());
            String url = Utils.fillTemplate(urlTemplate, values);

            HtmlPage page = HtmlUnitClientFactory.startDriver().getPage(url);
            DomElement forecastEl = page.getElementById("forecast");
            List<DayForecast> forecasts = new ArrayList<>();

            for (DomElement day : forecastEl.getChildElements()) {

                Optional<LocalDate> date = getHtmlElementDescendants(day, d -> hasCssClass(d, "fc_day_date")).stream()
                        .findFirst().map(d -> d.getTextContent().trim()).flatMap(d -> Utils.getFirstMatch(d, "\\d+"))
                        .map(Integer::parseInt)
                        .flatMap(this::gateDate);

                Optional<HtmlElement> totalCloudHourlyEl = getHtmlElementDescendants(day, d -> hasCssClass(d, "fc_detail_row")).stream().map(d -> (HtmlElement) d).findFirst();

                List<Integer> totalCloudCoverage = totalCloudHourlyEl.stream().flatMap(e -> e.getElementsByTagName("li").stream())
                        .map(e -> Integer.parseInt(e.getTextContent().trim()))
                        .collect(Collectors.toList());

                LocalDateTime dateTime = LocalDateTime.of(date.get(), LocalTime.MIDNIGHT);
                DayForecast dayForecast = new DayForecast(location.getLocationName(), dateTime, totalCloudCoverage, Collections.emptyList());

                forecasts.add(dayForecast);
            }

            return forecasts;

        } catch (Exception e) {
            log.error("Failed to scrape page ", e);
            return Collections.emptyList();
        }
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
