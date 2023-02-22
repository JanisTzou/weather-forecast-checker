package com.google.weatherforecastchecker.scraper.forcast;

import com.gargoylesoftware.htmlunit.html.DomNode;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.google.weatherforecastchecker.LocationsReader;
import com.google.weatherforecastchecker.Utils;
import com.google.weatherforecastchecker.htmlunit.HtmlUnitClientFactory;
import com.google.weatherforecastchecker.Location;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
public class MeteoBlueScraper implements ForecastScraper<Location> {

    // TODO add validation of the produced hourly forecast ...

    private final String urlTemplate;

    private static final Map<Integer, LocationsReader.MeteobluePictorgramsConfig> pictogramsConfigs = LocationsReader.getMeteobluePictogramsConfigs();

    public MeteoBlueScraper(@Value("${meteoblue.web.forecast.url.template}") String urlTemplate) {
        this.urlTemplate = urlTemplate;
    }

    @Override
    public List<DayForecast> scrape(List<Location> locations) {
        return locations.stream()
                .flatMap(l -> scrape(l).stream())
                .peek(f -> {
                    System.out.println(f);
                    Utils.sleep(5000);
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<DayForecast> scrape(Location config) {
        return IntStream.rangeClosed(1, 2) // 2 days scraping only
                .mapToObj(day -> scrape(config, day))
                .flatMap(Optional::stream)
                .collect(Collectors.toList());
    }

    public Optional<DayForecast> scrape(Location location, int day) {
        try {
            Map<String, Object> values = Map.of("lat", location.getLatitude(), "lon", location.getLongitude(), "day", day);
            String url = Utils.fillTemplate(urlTemplate, values);

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

            List<Integer> totalCloudCoverage = threeHourlyView.stream()
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
                    .flatMap(n -> IntStream.rangeClosed(1, 3).map(i -> pictogramsConfigs.get(n).getCloudCoverage()).boxed())
                    .collect(Collectors.toList());

            Optional<LocalDate> date = parseDate(dateTimeStr.get());

            LocalDateTime dateTime = LocalDateTime.of(date.get(), LocalTime.MIDNIGHT);

            DayForecast dayForecast = new DayForecast(location.getLocationName(), dateTime, totalCloudCoverage, Collections.emptyList());

            return Optional.of(dayForecast);

        } catch (Exception e) {
            log.error("Failed to scrape page ", e);
            return Optional.empty();
        }
    }

    private Optional<LocalDate> parseDate(String date) {
        return Optional.ofNullable(LocalDate.parse(date.substring(0, 10)));
    }

}
