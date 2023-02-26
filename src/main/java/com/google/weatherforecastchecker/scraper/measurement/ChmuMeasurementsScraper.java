package com.google.weatherforecastchecker.scraper.measurement;

import com.gargoylesoftware.htmlunit.html.DomNodeList;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.google.weatherforecastchecker.htmlunit.HtmlUnitClientFactory;
import com.google.weatherforecastchecker.scraper.LocationConfigRepository;
import com.google.weatherforecastchecker.scraper.ScrapingProps;
import com.google.weatherforecastchecker.scraper.Source;
import com.google.weatherforecastchecker.scraper.forecast.NonLocationBasedScraper;
import com.google.weatherforecastchecker.util.Utils;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Log4j2
@Component
@Profile({"chmu-oblacnost", "default"})
public class ChmuMeasurementsScraper implements NonLocationBasedScraper<CloudCoverageMeasurements> {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("d-M-yyyy H:mm");

    private final ChmuScraperProps properties;
    private final LocationConfigRepository locationConfigRepository;

    public ChmuMeasurementsScraper(ChmuScraperProps properties, LocationConfigRepository locationConfigRepository) {
        this.properties = properties;
        this.locationConfigRepository = locationConfigRepository;
    }

    @Override
    public Optional<CloudCoverageMeasurements> scrape() {
        try {
            HtmlPage page = HtmlUnitClientFactory.startDriver().getPage(properties.getUrl());
            List<HtmlElement> tbodyList = page.getElementsByTagName("tbody").stream().map(d -> (HtmlElement) d).collect(Collectors.toList());
            LocalDateTime dateTime = parseDateTime(tbodyList);
            List<CloudCoverageMeasurement> measurements = parseMeasurements(tbodyList, dateTime);
            if (!measurements.isEmpty()) {
                return Optional.of(new CloudCoverageMeasurements(measurements));
            }
        } catch (Exception e) {
            log.error("Failed to scrape page ", e);
        }
        return Optional.empty();
    }

    @Override
    public ScrapingProps getScrapingProps() {
        return properties;
    }

    @Override
    public Source getSource() {
        return Source.CHMU_WEB;
    }

    private List<CloudCoverageMeasurement> parseMeasurements(List<HtmlElement> tbodyList, LocalDateTime dateTime) {
        Optional<HtmlElement> table2 = tbodyList.stream().reduce((t1, t2) -> t2);
        List<CloudCoverageMeasurement> measurements = new ArrayList<>();

        if (table2.isPresent()) {
            Map<String, ChmuLocationConfig> locationsByStations = getLocationsByStations(locationConfigRepository.getChmuLocationConfigs());
            DomNodeList<HtmlElement> rows = table2.get().getElementsByTagName("tr");
            for (int rowNo = 1; rowNo < rows.size(); rowNo++) { // skip first row containing headers
                HtmlElement row = rows.get(rowNo);
                Optional<String> stanice = row.getElementsByTagName("td").stream().findFirst().map(e -> e.getTextContent().trim());
                if (stanice.isPresent()) {
                    Optional<String> pokrytiOblohyStr = row.getElementsByTagName("td").stream().limit(3).reduce((d1, d2) -> d2).map(e -> e.getTextContent().trim());
                    Optional<Integer> pokrytiOblohyInt = pokrytiOblohyStr.flatMap(d -> Utils.getFirstMatch(d, "\\d\\/\\d")).map(m -> m.split("/")[0]).map(Integer::parseInt);
                    Optional<Integer> pokrytiOblohyPercent = pokrytiOblohyInt.flatMap(this::fractionToPercentage);
                    ChmuLocationConfig location = locationsByStations.get(stanice.get());

                    if (location != null) {
                        CloudCoverageMeasurement measurement = new CloudCoverageMeasurement(
                                LocalDateTime.now(),
                                dateTime,
                                location.getLocation(),
                                pokrytiOblohyStr.orElse(""),
                                pokrytiOblohyPercent.orElse(null),
                                getSource()
                        );
                        measurements.add(measurement);
                    } else {
                        log.error("Failed to find location for station = {}", stanice.get());
                    }

                } else {
                    log.warn("Failed to scrape stanice!");
                }
            }
        }
        return measurements;
    }

    private LocalDateTime parseDateTime(List<HtmlElement> tbodyList) {
        Optional<HtmlElement> table1 = tbodyList.stream().findFirst();
        String dateTimeStr = null;
        if (table1.isPresent()) {
            Optional<HtmlElement> lastRow = table1.get().getElementsByTagName("tr").stream().reduce((r1, r2) -> r2);
            if (lastRow.isPresent()) {
                dateTimeStr = lastRow.get().getTextContent().trim();
            }
        }
        return parseDateTime(dateTimeStr).orElse(LocalDateTime.now().truncatedTo(ChronoUnit.HOURS));
    }

    private Optional<LocalDateTime> parseDateTime(String dateTime) {
        try {
            if (dateTime != null) {
                if (dateTime.length() >= 16) {
                    // example of datetime = "24. 2. 2023 21:00 SE�"
                    LocalDateTime parsed = LocalDateTime.parse(dateTime.substring(0, dateTime.indexOf("SE") - 1).replaceAll("\\. ", "-"), DATE_TIME_FORMATTER);
                    return Optional.of(parsed);
                }
            }
        } catch (Exception e) {
            log.error("Failed to parse date and time from {}", dateTime, e);
        }
        return Optional.empty();
    }

    /**
     * The response includes � replacement characters and to match the contained station
     * names to our locations we need to create this mapping
     */
    Map<String, ChmuLocationConfig> getLocationsByStations(List<ChmuLocationConfig> locationConfigs) {
        return locationConfigs.stream().collect(Collectors.toMap(ChmuLocationConfig::getStationName, c -> c));
    }

    Optional<Integer> fractionToPercentage(Integer eights) {
        if (eights != null) {
            return Optional.of(BigDecimal.valueOf(eights * 100).divide(BigDecimal.valueOf(8), RoundingMode.UP).intValue());
        }
        return Optional.empty();
    }

}
