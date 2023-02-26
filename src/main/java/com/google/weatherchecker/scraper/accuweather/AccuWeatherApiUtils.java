package com.google.weatherchecker.scraper.accuweather;

import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Log4j2
public class AccuWeatherApiUtils {

    public static void logRemainingRateLimit(ResponseEntity<?> resp) {
        List<String> headers = resp.getHeaders().get("RateLimit-Remaining");
        if (headers != null) {
            Optional<String> remainingRateLimit = headers.stream().findFirst();
            remainingRateLimit.ifPresent(s -> log.info("Remaining rate limit = {}", s));
        } else {
            String allHeaders = resp.getHeaders().entrySet().stream()
                    .map(e -> e.getKey() + ": " + e.getValue().stream()
                            .collect(Collectors.joining(","))
                    ).collect(Collectors.joining("; "));
            log.info("Received headers: {}", allHeaders);
        }
    }

}
