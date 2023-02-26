package com.google.weatherchecker.scraper;

import com.google.weatherchecker.model.Source;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class Schedulers {

    private final Map<Source, Scheduler> schedulers = new HashMap<>();

    public Scheduler getScheduler(Source source) {
        return schedulers.computeIfAbsent(source, s -> new Scheduler());
    }

}
