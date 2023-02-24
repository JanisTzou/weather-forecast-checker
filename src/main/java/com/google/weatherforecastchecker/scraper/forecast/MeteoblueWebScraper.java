package com.google.weatherforecastchecker.scraper.forecast;

import com.gargoylesoftware.htmlunit.html.DomNode;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.google.weatherforecastchecker.LocationConfig;
import com.google.weatherforecastchecker.LocationConfigRepository;
import com.google.weatherforecastchecker.MeteobluePictorgramsConfig;
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
import java.util.stream.Stream;

import static com.google.weatherforecastchecker.htmlunit.HtmlUnitUtils.getHtmlElementDescendants;
import static com.google.weatherforecastchecker.htmlunit.HtmlUnitUtils.hasCssClass;

/**
 * On 3-hour intervals from Meteoblue site:
 * The period for which the pictogram is effective is printed above or below. An hour indicates the end of the interval
 * (e.g. a 3-hour pictogram marked 11:00 includes the forecast from 8:01 until 11:00,
 * and the following 3-hour pictogram marked 14:00 includes the forecast from 11:01 until 14:00)
 * <p>
 * Source:
 * https://content.meteoblue.com/en/research-education/specifications/standards/symbols-and-pictograms
 */
@Log4j2
@Component
@Profile("meteoblue-web")
public class MeteoblueWebScraper implements ForecastScraper<LocationConfig> {

    // TODO add validation of the produced hourly forecast ...

    private final Properties properties;

    private static final Map<Integer, MeteobluePictorgramsConfig> pictogramsConfigs = LocationConfigRepository.getMeteobluePictogramsConfigs();

    public MeteoblueWebScraper(Properties properties) {
        this.properties = properties;
    }

    @PostConstruct
    public void scrape() {
        List<LocationConfig> locations = LocationConfigRepository.getLocationConfigs(getSource());
        scrape(locations);
    }

    @Override
    public Optional<Forecast> scrape(LocationConfig locationConfig) {
        List<HourForecast> hourForecasts = IntStream.rangeClosed(1, properties.getDays())
                .mapToObj(day -> scrape(locationConfig, day))
                .flatMap(List::stream)
                .collect(Collectors.toList());
        if (!hourForecasts.isEmpty()) {
            return Optional.of(new Forecast(getSource(), locationConfig.getName(), hourForecasts));
        }
        return Optional.empty();
    }

    @Override
    public Source getSource() {
        return Source.METEOBLUE_WEB;
    }

    @Override
    public ScrapingProperties getScrapingProperties() {
        return properties;
    }

    public List<HourForecast> scrape(LocationConfig location, int day) {
        try {
            Map<String, Object> values = Map.of("lat", location.getLatitude(), "lon", location.getLongitude(), "day", day);
            String url = Utils.fillTemplate(properties.getUrl(), values);

            HtmlPage page = HtmlUnitClientFactory.startDriver().getPage(url);

            // TODO maybe do the same with xPaths ?
            List<DomNode> threeHourlyView = getHtmlElementDescendants(page, d -> hasCssClass(d, "three-hourly-view"));

            Optional<String> dateTimeStr = threeHourlyView.stream()
                    .findFirst()
                    .flatMap(e -> ((HtmlElement) e).getElementsByTagName("th").stream().findFirst())
                    .stream()
                    .findFirst()
                    .flatMap(e -> e.getElementsByTagName("time").stream().findFirst())
                    .stream()
                    .map(e -> e.getAttribute("datetime"))
                    .findFirst();

            // should get 24 values // TODO validation
            List<Integer> hourCloudCoverages = threeHourlyView.stream()
                    .findFirst().stream().flatMap(e -> ((HtmlElement) e).getElementsByTagName("tr").stream())
                    .filter(e -> hasCssClass(e, "icons"))
                    .findFirst()
                    .map(e -> e.getElementsByTagName("img"))
                    .stream()
                    .flatMap(Collection::stream)
                    .flatMap(e -> {
                        String pictogramUrl = e.getAttribute("src");
                        Optional<Integer> number = Utils.getFirstMatch(pictogramUrl, "\\d+_[day|night]").map(m -> m.split("_")[0]).map(Integer::parseInt);
                        return number.stream();
                    })
                    // TODO the descriptions of pictorgrams would be good as well to have ... so do not get the mapping of the coverage here ...
                    .flatMap(this::convertThreeHourlyToOneHourly)
                    .collect(Collectors.toList());

            Optional<LocalDate> date = parseDate(dateTimeStr.get());

            LocalDateTime dateTime = LocalDateTime.of(date.get(), LocalTime.MIDNIGHT);

            return IntStream.rangeClosed(0, hourCloudCoverages.size() - 1)
                    .mapToObj(hourNo -> new HourForecast(dateTime.plusHours(hourNo), hourCloudCoverages.get(hourNo), null))
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Failed to scrape page ", e);
        }
        return Collections.emptyList();
    }

    private Stream<Integer> convertThreeHourlyToOneHourly(Integer n) {
        return IntStream.rangeClosed(1, 3).map(i -> pictogramsConfigs.get(n).getCloudCoverage()).boxed();
    }

    private Optional<LocalDate> parseDate(String date) {
        return Optional.ofNullable(LocalDate.parse(date.substring(0, 10)));
    }

    @ConfigurationProperties("meteoblue.web.forecast")
    public static class Properties extends ScrapingProperties {
    }

}
