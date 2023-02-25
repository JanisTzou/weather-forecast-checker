package com.google.weatherforecastchecker.scraper;

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
