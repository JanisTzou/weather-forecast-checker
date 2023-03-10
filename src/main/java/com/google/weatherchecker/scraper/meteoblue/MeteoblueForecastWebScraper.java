package com.google.weatherchecker.scraper.meteoblue;

import com.gargoylesoftware.htmlunit.html.DomNode;
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
import java.util.stream.Stream;

import static com.google.weatherchecker.htmlunit.HtmlUnitUtils.getHtmlElementDescendants;
import static com.google.weatherchecker.htmlunit.HtmlUnitUtils.hasCssClass;

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
@Profile({"meteoblue-web", "default"})
public class MeteoblueForecastWebScraper implements ForecastScraper<LocationConfig> {

    // TODO add validation of the produced hourly forecast ...

    private final MeteoblueForecastWebScraperProps props;
    private final LocationConfigRepository locationConfigRepository;

    public MeteoblueForecastWebScraper(MeteoblueForecastWebScraperProps props,
                                       LocationConfigRepository locationConfigRepository) {
        this.props = props;
        this.locationConfigRepository = locationConfigRepository;
    }

    @Override
    public Optional<Forecast> scrape(LocationConfig location) {
        List<HourlyForecast> hourlyForecasts = IntStream.rangeClosed(1, props.getDays())
                .mapToObj(day -> scrape(location, day))
                .flatMap(List::stream)
                .collect(Collectors.toList());
        if (!hourlyForecasts.isEmpty()) {
            return Optional.of(new Forecast(LocalDateTime.now(), getSource(), location.getLocation(), hourlyForecasts));
        }
        return Optional.empty();
    }

    @Override
    public List<LocationConfig> getLocationConfigs() {
        return locationConfigRepository.getLocationConfigs(getSource());
    }

    @Override
    public Source getSource() {
        return Source.METEOBLUE_WEB;
    }

    @Override
    public ForecastScrapingProps getScrapingProps() {
        return props;
    }

    public List<HourlyForecast> scrape(LocationConfig location, int day) {
        try {
            Map<String, Object> values = Map.of("lat", location.getLatitude(), "lon", location.getLongitude(), "day", day);
            String url = Utils.fillTemplate(props.getUrl(), values);

            HtmlPage page = HtmlUnitClientFactory.startDriver().getPage(url);

            Map<Integer, MeteobluePictorgramsConfig> pictogramsConfigs = locationConfigRepository.getMeteobluePictogramsConfigs();

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

            // should get 24 values - each for 8 from the web and we "spread" x3 ...
            List<Integer> iconNumbers = threeHourlyView.stream()
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
                    .flatMap(this::convertThreeHourlyToOneHourly)
                    .collect(Collectors.toList());

            if (iconNumbers.size() == 24 && dateTimeStr.isPresent()) {
                Optional<LocalDate> date = parseDate(dateTimeStr.get());
                if (date.isPresent()) {
                    LocalDateTime dateTime = LocalDateTime.of(date.get(), LocalTime.MIDNIGHT);
                    return IntStream.rangeClosed(0, iconNumbers.size() - 1)
                            .mapToObj(hourNo -> {
                                Integer iconNo = iconNumbers.get(hourNo);
                                Integer cloudCoverageTotal = getCloudCoverageTotal(pictogramsConfigs, iconNo);
                                LocalDateTime hour = dateTime.plusHours(hourNo + 1); // need +1 to start at 01:00 as that matches the times returned by the Meteoblue API
                                return new HourlyForecast(hour, cloudCoverageTotal, "icon_" + iconNo);
                            })
                            .collect(Collectors.toList());
                } else {
                    log.error("Failed to parse dateTime = {}", dateTimeStr.orElse(null));
                }
            } else {
                log.error("Failed to scrape data: found icon count = {}, scraped dateTime = {}", iconNumbers.size(), dateTimeStr.orElse(null));
            }


        } catch (Exception e) {
            log.error("Failed to scrape page ", e);
        }
        return Collections.emptyList();
    }

    private Integer getCloudCoverageTotal(Map<Integer, MeteobluePictorgramsConfig> pictogramsConfigs, Integer iconNo) {
        MeteobluePictorgramsConfig config = pictogramsConfigs.get(iconNo);
        Integer cloudCoverageTotal;
        if (config == null) {
            log.error("Failed to find pictogram config for icon no = {}", iconNo);
            cloudCoverageTotal = null;
        } else {
            cloudCoverageTotal = config.getCloudCoverage();
        }
        return cloudCoverageTotal;
    }

    private Stream<Integer> convertThreeHourlyToOneHourly(Integer iconNo) {
        return IntStream.rangeClosed(1, 3).map(i -> iconNo).boxed();
    }

    private Optional<LocalDate> parseDate(String date) {
        return Optional.ofNullable(LocalDate.parse(date.substring(0, 10)));
    }

}
