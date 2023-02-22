package com.google.weatherforecastchecker.scraper.measurement;

import lombok.Data;

@Data
public class CloudCoverageMeasurement {

    private final String dateTime;
    private final String location;
    private final String description;
    private final Integer covered; // 0/8 - 8/8 ... jasno ... zatazeno -> convert to %

}
