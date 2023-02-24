package com.google.weatherforecastchecker.scraper.forecast;

import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.google.weatherforecastchecker.LocationConfig;
import com.google.weatherforecastchecker.LocationConfigRepository;
import com.google.weatherforecastchecker.Utils;
import com.google.weatherforecastchecker.htmlunit.HtmlUnitClientFactory;
import jakarta.annotation.PostConstruct;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.google.weatherforecastchecker.htmlunit.HtmlUnitUtils.getHtmlElementDescendants;
import static com.google.weatherforecastchecker.htmlunit.HtmlUnitUtils.hasCssClass;

@Log4j2
@Component
@Profile("clearoutside")
public class ClearOutsideWebScraper implements ForecastScraper<LocationConfig> {

    private final Properties properties;

    public ClearOutsideWebScraper(Properties properties) {
        this.properties = properties;
    }

    @PostConstruct
    public void scrape() {
        List<LocationConfig> locations = LocationConfigRepository.getLocationConfigs(getSource());
        scrape(locations);
    }

    @Override
    public Optional<Forecast> scrape(LocationConfig locationConfig) {
        try {
            Map<String, Object> values = Map.of("lat", locationConfig.getLatitude(), "lon", locationConfig.getLongitude());
            String url = Utils.fillTemplate(properties.getUrl(), values);

            HtmlPage page = HtmlUnitClientFactory.startDriver().getPage(url);
            DomElement forecastEl = page.getElementById("forecast");

            List<HourForecast> hourForecasts = new ArrayList<>();

            int count = 0;
            for (DomElement day : forecastEl.getChildElements()) {
                count++;
                if (count <= properties.getDays()) {
                    Optional<LocalDate> date = getHtmlElementDescendants(day, d -> hasCssClass(d, "fc_day_date")).stream()
                            .findFirst().map(d -> d.getTextContent().trim()).flatMap(d -> Utils.getFirstMatch(d, "\\d+"))
                            .map(Integer::parseInt)
                            .flatMap(this::gateDate);

                    Optional<HtmlElement> totalCloudHourlyEl = getHtmlElementDescendants(day, d -> hasCssClass(d, "fc_detail_row")).stream().map(d -> (HtmlElement) d).findFirst();

                    // expected 24 values ... TODO validation
                    List<Integer> hourCloudCoverages = totalCloudHourlyEl.stream().flatMap(e -> e.getElementsByTagName("li").stream())
                            .map(e -> Integer.parseInt(e.getTextContent().trim()))
                            .collect(Collectors.toList());

                    if (date.isPresent()) {
                        LocalDateTime dateTime = LocalDateTime.of(date.get(), LocalTime.MIDNIGHT);

                        List<HourForecast> hourForcastsForDay = IntStream.rangeClosed(0, hourCloudCoverages.size() - 1)
                                .mapToObj(hourNo -> new HourForecast(dateTime.plusHours(hourNo), hourCloudCoverages.get(hourNo), null))
                                .collect(Collectors.toList());

                        hourForecasts.addAll(hourForcastsForDay);
                    } else {
                        log.warn("Failed to parse date!");
                    }
                }
            }

            return Optional.of(new Forecast(getSource(), locationConfig.getName(), hourForecasts));

        } catch (Exception e) {
            log.error("Failed to scrape page ", e);
        }

        return Optional.empty();
    }

    @Override
    public Source getSource() {
        return Source.CLEAR_OUTSIDE_WEB;
    }

    @Override
    public ScrapingProperties getScrapingProperties() {
        return properties;
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

    @ConfigurationProperties("clearoutside.web.forecast")
    public static class Properties extends ScrapingProperties {
    }

}
