package com.google.weatherforecastchecker.scraper.measurement;

import com.gargoylesoftware.htmlunit.html.DomNodeList;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.google.weatherforecastchecker.Utils;
import com.google.weatherforecastchecker.htmlunit.HtmlUnitClientFactory;
import lombok.extern.log4j.Log4j2;

import java.util.*;
import java.util.stream.Collectors;

@Log4j2
public class ChmuScraper {

    public static void main(String[] args) {
        new ChmuScraper().scrape();

//        String text = "P�ehled";
//        System.out.println(text);
//        text.chars().forEach(i -> System.out.println(i));

    }

    // TODO this will need to be JS ...
    public void scrape() {
        try {
            String url = "https://www.chmi.cz/files/portal/docs/meteo/opss/pocasicko_nove/st_oblacnost_cz.html";

            HtmlPage page = HtmlUnitClientFactory.startDriver().getPage(url);

            List<HtmlElement> tbodyList = page.getElementsByTagName("tbody").stream().map(d -> (HtmlElement) d).collect(Collectors.toList());
            Optional<HtmlElement> table1 = tbodyList.stream().findFirst();

            String dateTime = null;
            if (table1.isPresent()) {
                Optional<HtmlElement> lastRow = table1.get().getElementsByTagName("tr").stream().reduce((r1, r2) -> r2);
                if (lastRow.isPresent()) {
                    dateTime = lastRow.get().getTextContent().trim();
//                    System.out.println(lastRow.get().getTextContent().trim());
                }
            }

            Optional<HtmlElement> table2 = tbodyList.stream().reduce((t1, t2) -> t2);

            List<CloudCoverageMeasurement> cloudCoverageMeasurements = new ArrayList<>();
            if (table2.isPresent()) {
                DomNodeList<HtmlElement> rows = table2.get().getElementsByTagName("tr");
                for (int rowNo = 1; rowNo < rows.size(); rowNo++) { // skip first row containing headers
                    HtmlElement row = rows.get(rowNo);
                    Optional<String> stanice = row.getElementsByTagName("td").stream().findFirst().map(e -> e.getTextContent().trim());
                    Optional<String> pokrytiObohy = row.getElementsByTagName("td").stream().limit(3).reduce((d1, d2) -> d2).map(e -> e.getTextContent().trim());
                    Optional<Integer> pokrytiOblohyCislo = pokrytiObohy.flatMap(d -> Utils.getFirstMatch(d, "\\d\\/\\d")).map(m -> m.split("\\/")[0]).map(Integer::parseInt);
//                    log.info("stanice = {}, pokryti oblohy = {}", stanice.get(), pokrytiObohy.get());
                    CloudCoverageMeasurement cloudCoverageMeasurement = new CloudCoverageMeasurement(dateTime, stanice.get(), pokrytiObohy.get(), pokrytiOblohyCislo.orElse(null));
                    System.out.println(cloudCoverageMeasurement);
                }
            }

        } catch (Exception e) {
            log.error("Failed to scrape page ", e);
        }

    }


}
