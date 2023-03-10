package com.google.weatherchecker.scraper.clearoutside;

import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.google.weatherchecker.htmlunit.HtmlUnitClientFactory;
import com.google.weatherchecker.scraper.ForecastScrapingProps;
import com.google.weatherchecker.scraper.LocationConfig;
import com.google.weatherchecker.scraper.LocationConfigRepository;
import com.google.weatherchecker.model.Source;
import com.google.weatherchecker.model.Forecast;
import com.google.weatherchecker.scraper.ForecastScraper;
import com.google.weatherchecker.model.HourlyForecast;
import com.google.weatherchecker.util.Utils;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.google.weatherchecker.htmlunit.HtmlUnitUtils.getHtmlElementDescendants;
import static com.google.weatherchecker.htmlunit.HtmlUnitUtils.hasCssClass;

@Log4j2
@Component
@Profile({"clearoutside", "default"})
public class ClearOutsideForecastWebScraper implements ForecastScraper<LocationConfig> {

    private final ClearOutsideForecastWebScraperProps properties;
    private final LocationConfigRepository locationConfigRepository;

    public ClearOutsideForecastWebScraper(ClearOutsideForecastWebScraperProps properties, LocationConfigRepository locationConfigRepository) {
        this.properties = properties;
        this.locationConfigRepository = locationConfigRepository;
    }

    @Override
    public Optional<Forecast> scrape(LocationConfig location) {
        try {
            Map<String, Object> values = Map.of("lat", location.getLatitude(), "lon", location.getLongitude());
            String url = Utils.fillTemplate(properties.getUrl(), values);

            HtmlPage page = HtmlUnitClientFactory.startDriver().getPage(url);
            DomElement forecastEl = page.getElementById("forecast");

            List<HourlyForecast> hourlyForecasts = new ArrayList<>();

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

                        List<HourlyForecast> hourForcastsForDay = IntStream.rangeClosed(0, hourCloudCoverages.size() - 1)
                                .mapToObj(hourNo -> new HourlyForecast(dateTime.plusHours(hourNo), hourCloudCoverages.get(hourNo), null))
                                .collect(Collectors.toList());

                        hourlyForecasts.addAll(hourForcastsForDay);
                    } else {
                        log.warn("Failed to parse date!");
                    }
                }
            }

            return Optional.of(new Forecast(LocalDateTime.now(), getSource(), location.getLocation(), hourlyForecasts));

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
        return Source.CLEAR_OUTSIDE_WEB;
    }

    @Override
    public ForecastScrapingProps getScrapingProps() {
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

}
