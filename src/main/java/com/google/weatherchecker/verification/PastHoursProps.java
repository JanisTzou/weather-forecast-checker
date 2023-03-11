package com.google.weatherchecker.verification;

import com.google.weatherchecker.scraper.ScrapingProps;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Data
@ConfigurationProperties("verifications.precalculated")
public class PastHoursProps {

    // TODO add validation
    private String pastHoursCommaSeparated;

    public List<Integer> getPastHours() {
        return Arrays.stream(pastHoursCommaSeparated.split(",")).map(Integer::parseInt).collect(Collectors.toList());
    }

}
